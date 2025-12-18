package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.ListaPresenca;
import com.mocs_on.domain.RegistroPresenca;
import com.mocs_on.dto.ComiteResumoDTO;

@Repository
public class PresencaDao {

    private final JdbcTemplate jdbc;

    public PresencaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ComiteResumoDTO> listarComites() {
        String sql = "SELECT id, nome, sigla FROM comites ORDER BY nome";
        return jdbc.query(sql, (rs, rowNum) -> new ComiteResumoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("sigla")
        ));
    }

    public List<ListaPresenca> listarListas() {
        String sql = """
                SELECT l.id,
                       l.comite_id,
                       l.titulo,
                       l.data_sessao,
                       l.hora_inicio,
                       l.hora_fim,
                       l.observacao,
                       l.criado_por,
                       l.criado_em,
                       c.nome AS comite_nome,
                       c.sigla AS comite_sigla
                FROM presenca_listas l
                LEFT JOIN comites c ON c.id = l.comite_id
                ORDER BY l.data_sessao DESC, l.hora_inicio DESC, l.id DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> mapLista(rs));
    }

    public ListaPresenca buscarLista(long id) {
        String sql = """
                SELECT l.id,
                       l.comite_id,
                       l.titulo,
                       l.data_sessao,
                       l.hora_inicio,
                       l.hora_fim,
                       l.observacao,
                       l.criado_por,
                       l.criado_em,
                       c.nome AS comite_nome,
                       c.sigla AS comite_sigla
                FROM presenca_listas l
                LEFT JOIN comites c ON c.id = l.comite_id
                WHERE l.id = ?
                """;
        List<ListaPresenca> listas = jdbc.query(sql, (rs, rowNum) -> mapLista(rs), id);
        return listas.stream().findFirst().orElse(null);
    }

    public long inserirLista(ListaPresenca lista) {
        String sql = """
                INSERT INTO presenca_listas (comite_id, titulo, data_sessao, hora_inicio, hora_fim, observacao, criado_por)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql,
                lista.getComiteId(),
                lista.getTitulo(),
                lista.getDataSessao(),
                lista.getHoraInicio(),
                lista.getHoraFim(),
                lista.getObservacao(),
                lista.getCriadoPor()
        );
        Long id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id == null ? 0L : id;
    }

    public List<RegistroPresenca> listarRegistros(long listaId) {
        String sql = """
                SELECT id, lista_id, usuario_id, usuario_nome, usuario_email, status, observacao
                FROM presenca_registros
                WHERE lista_id = ?
                ORDER BY usuario_nome
                """;
        return jdbc.query(sql, (rs, rowNum) -> mapRegistro(rs), listaId);
    }

    public List<RegistroPresenca> listarDelegados() {
        String sql = """
                SELECT id, nome, email
                FROM usuarios
                WHERE UPPER(tipo) = 'DELEGADO'
                ORDER BY nome
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            RegistroPresenca registro = new RegistroPresenca();
            registro.setUsuarioId(rs.getLong("id"));
            registro.setUsuarioNome(rs.getString("nome"));
            registro.setUsuarioEmail(rs.getString("email"));
            registro.setStatus("AUSENTE");
            return registro;
        });
    }

    public void inserirRegistros(long listaId, List<RegistroPresenca> registros) {
        if (registros == null || registros.isEmpty()) {
            return;
        }
        String sql = """
                INSERT INTO presenca_registros (lista_id, usuario_id, usuario_nome, usuario_email, status, observacao)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RegistroPresenca registro = registros.get(i);
                ps.setLong(1, listaId);
                if (registro.getUsuarioId() != null) {
                    ps.setLong(2, registro.getUsuarioId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setString(3, registro.getUsuarioNome());
                ps.setString(4, registro.getUsuarioEmail());
                ps.setString(5, normalizeStatus(registro.getStatus()));
                ps.setString(6, registro.getObservacao());
            }

            @Override
            public int getBatchSize() {
                return registros.size();
            }
        });
    }

    public int atualizarRegistro(long listaId, RegistroPresenca registro) {
        if (registro.getUsuarioId() == null) {
            return 0;
        }
        String sql = """
                UPDATE presenca_registros
                SET status = ?, observacao = ?, usuario_nome = ?, usuario_email = ?
                WHERE lista_id = ? AND usuario_id = ?
                """;
        return jdbc.update(sql,
                normalizeStatus(registro.getStatus()),
                registro.getObservacao(),
                registro.getUsuarioNome(),
                registro.getUsuarioEmail(),
                listaId,
                registro.getUsuarioId()
        );
    }

    public void inserirRegistro(long listaId, RegistroPresenca registro) {
        String sql = """
                INSERT INTO presenca_registros (lista_id, usuario_id, usuario_nome, usuario_email, status, observacao)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbc.update(sql,
                listaId,
                registro.getUsuarioId(),
                registro.getUsuarioNome(),
                registro.getUsuarioEmail(),
                normalizeStatus(registro.getStatus()),
                registro.getObservacao()
        );
    }

    private ListaPresenca mapLista(java.sql.ResultSet rs) throws SQLException {
        ListaPresenca lista = new ListaPresenca();
        lista.setId(rs.getLong("id"));
        long comiteId = rs.getLong("comite_id");
        if (!rs.wasNull()) {
            lista.setComiteId(comiteId);
        }
        lista.setTitulo(rs.getString("titulo"));
        lista.setDataSessao(rs.getString("data_sessao"));
        lista.setHoraInicio(rs.getString("hora_inicio"));
        lista.setHoraFim(rs.getString("hora_fim"));
        lista.setObservacao(rs.getString("observacao"));
        lista.setCriadoPor(rs.getString("criado_por"));
        lista.setCriadoEm(rs.getString("criado_em"));
        lista.setComiteNome(rs.getString("comite_nome"));
        lista.setComiteSigla(rs.getString("comite_sigla"));
        return lista;
    }

    private RegistroPresenca mapRegistro(java.sql.ResultSet rs) throws SQLException {
        RegistroPresenca registro = new RegistroPresenca();
        registro.setId(rs.getLong("id"));
        registro.setListaId(rs.getLong("lista_id"));
        long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) {
            registro.setUsuarioId(usuarioId);
        }
        registro.setUsuarioNome(rs.getString("usuario_nome"));
        registro.setUsuarioEmail(rs.getString("usuario_email"));
        registro.setStatus(rs.getString("status"));
        registro.setObservacao(rs.getString("observacao"));
        return registro;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "AUSENTE";
        }
        return status.trim().toUpperCase();
    }
}
