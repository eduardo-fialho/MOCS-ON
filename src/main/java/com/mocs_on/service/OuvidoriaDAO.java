package com.mocs_on.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.OuvidoriaRelato;

@Repository
public class OuvidoriaDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long inserirRelato(OuvidoriaRelato r) {
        String sql = """
            INSERT INTO ouvidoria_relatos (
              identificacao, nome_relator, comite_relator,
              categoria_relato,
              comite_conducao, comite_respeito, comite_imparcialidade, comite_apoio, comite_mensagem,
              secretariado_positivos, secretariado_negativos, secretariado_falta, secretariado_sugestoes,
              outros_mensagem, status, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'novo', ?)
        """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getIdentificacao());
            ps.setString(2, r.getNomeRelator());
            ps.setString(3, r.getComiteRelator());
            ps.setString(4, r.getCategoriaRelato());
            ps.setString(5, r.getComiteConducao());
            ps.setString(6, r.getComiteRespeito());
            ps.setString(7, r.getComiteImparcialidade());
            ps.setString(8, r.getComiteApoio());
            ps.setString(9, r.getComiteMensagem());
            ps.setString(10, r.getSecretariadoPositivos());
            ps.setString(11, r.getSecretariadoNegativos());
            ps.setString(12, r.getSecretariadoFalta());
            ps.setString(13, r.getSecretariadoSugestoes());
            ps.setString(14, r.getOutrosMensagem());
            ps.setTimestamp(15, now);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public java.util.List<OuvidoriaRelato> recuperarTodos() {
        String sql = """
            SELECT id, created_at, status, identificacao, nome_relator, comite_relator,
                   categoria_relato, comite_conducao, comite_respeito, comite_imparcialidade, comite_apoio, comite_mensagem,
                   secretariado_positivos, secretariado_negativos, secretariado_falta, secretariado_sugestoes,
                   outros_mensagem
            FROM ouvidoria_relatos
            ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OuvidoriaRelato r = new OuvidoriaRelato();
            r.setId(rs.getLong("id"));
            Timestamp ts = rs.getTimestamp("created_at");
            r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
            r.setStatus(rs.getString("status"));
            r.setIdentificacao(rs.getString("identificacao"));
            r.setNomeRelator(rs.getString("nome_relator"));
            r.setComiteRelator(rs.getString("comite_relator"));
            r.setCategoriaRelato(rs.getString("categoria_relato"));
            r.setComiteConducao(rs.getString("comite_conducao"));
            r.setComiteRespeito(rs.getString("comite_respeito"));
            r.setComiteImparcialidade(rs.getString("comite_imparcialidade"));
            r.setComiteApoio(rs.getString("comite_apoio"));
            r.setComiteMensagem(rs.getString("comite_mensagem"));
            r.setSecretariadoPositivos(rs.getString("secretariado_positivos"));
            r.setSecretariadoNegativos(rs.getString("secretariado_negativos"));
            r.setSecretariadoFalta(rs.getString("secretariado_falta"));
            r.setSecretariadoSugestoes(rs.getString("secretariado_sugestoes"));
            r.setOutrosMensagem(rs.getString("outros_mensagem"));
            return r;
        });
    }

    public OuvidoriaRelato recuperarPorId(Long id) {
        String sql = """
            SELECT id, created_at, status, identificacao, nome_relator, comite_relator,
                   categoria_relato, comite_conducao, comite_respeito, comite_imparcialidade, comite_apoio, comite_mensagem,
                   secretariado_positivos, secretariado_negativos, secretariado_falta, secretariado_sugestoes,
                   outros_mensagem
            FROM ouvidoria_relatos
            WHERE id = ?
        """;

        java.util.List<OuvidoriaRelato> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            OuvidoriaRelato r = new OuvidoriaRelato();
            r.setId(rs.getLong("id"));
            Timestamp ts = rs.getTimestamp("created_at");
            r.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
            r.setStatus(rs.getString("status"));
            r.setIdentificacao(rs.getString("identificacao"));
            r.setNomeRelator(rs.getString("nome_relator"));
            r.setComiteRelator(rs.getString("comite_relator"));
            r.setCategoriaRelato(rs.getString("categoria_relato"));
            r.setComiteConducao(rs.getString("comite_conducao"));
            r.setComiteRespeito(rs.getString("comite_respeito"));
            r.setComiteImparcialidade(rs.getString("comite_imparcialidade"));
            r.setComiteApoio(rs.getString("comite_apoio"));
            r.setComiteMensagem(rs.getString("comite_mensagem"));
            r.setSecretariadoPositivos(rs.getString("secretariado_positivos"));
            r.setSecretariadoNegativos(rs.getString("secretariado_negativos"));
            r.setSecretariadoFalta(rs.getString("secretariado_falta"));
            r.setSecretariadoSugestoes(rs.getString("secretariado_sugestoes"));
            r.setOutrosMensagem(rs.getString("outros_mensagem"));
            return r;
        }, id);

        return list.stream().findFirst().orElse(null);
    }
}
