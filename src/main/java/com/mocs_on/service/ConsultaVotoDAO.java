package com.mocs_on.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.TipoVoto;
import com.mocs_on.domain.ConsultaVoto;

@Repository
public class ConsultaVotoDAO {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaVotoDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrarVoto(Long consultaId, String usuarioUsername, TipoVoto voto) {
        String sql = """
            INSERT INTO consulta_votos
            (consulta_id, usuario_username, voto)
            VALUES (?, ?, ?)
        """;

        jdbcTemplate.update(sql, consultaId, usuarioUsername, voto.name());
    }

    public void atualizarVoto(Long consultaId, String usuarioUsername, TipoVoto voto) {
        String sql = """
            UPDATE consulta_votos
            SET voto = ?
            WHERE consulta_id = ?
            AND usuario_username = ?
        """;

        jdbcTemplate.update(sql, voto.name(), consultaId, usuarioUsername);
    }

    public boolean existeVoto(Long consultaId, String usuarioUsername) {
        String sql = """
            SELECT COUNT(*)
            FROM consulta_votos
            WHERE consulta_id = ?
            AND usuario_username = ?
        """;

        Integer count = jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            consultaId,
            usuarioUsername
        );

        return count != null && count > 0;
    }

    public ConsultaVoto buscarPorConsultaEUsuario(Long consultaId, String usuarioUsername) {
        String sql = """
            SELECT *
            FROM consulta_votos
            WHERE consulta_id = ?
            AND usuario_username = ?
        """;

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) return null;

            ConsultaVoto v = new ConsultaVoto();
            v.setId(rs.getLong("id"));
            v.setConsultaId(rs.getLong("consulta_id"));
            v.setUsuarioUsername(rs.getString("usuario_username"));
            v.setVoto(TipoVoto.valueOf(rs.getString("voto")));
            return v;
        }, consultaId, usuarioUsername);
    }

    public int contarVotos(Long consultaId, TipoVoto voto) {
        String sql = """
            SELECT COUNT(*)
            FROM consulta_votos
            WHERE consulta_id = ?
            AND voto = ?
        """;

        return jdbcTemplate.queryForObject(
            sql,
            Integer.class,
            consultaId,
            voto.name()
        );
    }
}
