package com.mocs_on.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mocs_on.domain.RelatoOuvidoria;
import com.mocs_on.domain.StatusRelatoOuvidoria;

@Repository
public class RelatoOuvidoriaDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<RelatoOuvidoria> recuperarTodos() {
        String sql = """
            SELECT *
            FROM relato_ouvidoria
            WHERE ativo = true
            ORDER BY criado_em DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs));
    }

    public RelatoOuvidoria recuperarPorId(Long id) {
        String sql = """
            SELECT *
            FROM relato_ouvidoria
            WHERE id = ? AND ativo = true
        """;

        List<RelatoOuvidoria> lista =
                jdbcTemplate.query(sql, (rs, rowNum) -> mapear(rs), id);

        return lista.stream().findFirst().orElse(null);
    }

    @Transactional
    public Long criar(RelatoOuvidoria r) {
        String sql = """
            INSERT INTO relato_ouvidoria
            (criado_em, ativo, status, autor, assunto, relato)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setTimestamp(1, Timestamp.valueOf(r.getCriadoEm()));
            ps.setBoolean(2, true);
            ps.setString(3, r.getStatus().name());
            ps.setString(4, r.getAutor());
            ps.setString(5, r.getAssunto());
            ps.setString(6, r.getRelato());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Transactional
    public int responder(Long id, String ouvidor, String resposta, StatusRelatoOuvidoria status) {
        String sql = """
            UPDATE relato_ouvidoria
            SET ouvidor = ?,
                resposta = ?,
                status = ?,
                resolvido_em = ?,
                ativo = true
            WHERE id = ?
        """;

        return jdbcTemplate.update(
                sql,
                ouvidor,
                resposta,
                status.name(),
                LocalDateTime.now(),
                id
        );
    }

    @Transactional
    public int desativar(Long id) {
        String sql = """
            UPDATE relato_ouvidoria
            SET ativo = false
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, id);
    }

    private RelatoOuvidoria mapear(java.sql.ResultSet rs) throws java.sql.SQLException {
        RelatoOuvidoria r = new RelatoOuvidoria();
        r.setId(rs.getLong("id"));
        r.setAutor(rs.getString("autor"));
        r.setAssunto(rs.getString("assunto"));
        r.setRelato(rs.getString("relato"));
        r.setOuvidor(rs.getString("ouvidor"));
        r.setResposta(rs.getString("resposta"));
        r.setAtivo(rs.getBoolean("ativo"));
        r.setStatus(StatusRelatoOuvidoria.valueOf(rs.getString("status")));

        Timestamp criado = rs.getTimestamp("criado_em");
        r.setCriadoEm(criado != null ? criado.toLocalDateTime() : null);

        Timestamp resolvido = rs.getTimestamp("resolvido_em");
        r.setResolvidoEm(resolvido != null ? resolvido.toLocalDateTime() : null);

        return r;
    }
}
