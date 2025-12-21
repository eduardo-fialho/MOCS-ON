package com.mocs_on.service;

import com.mocs_on.domain.MateriaLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MateriaLogDAO {

    private final JdbcTemplate jdbcTemplate;

    public MateriaLogDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void registrar(MateriaLog log) {

        String sql = """
            INSERT INTO materia_logs
            (materia_id, acao, usuario, descricao)
            VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.update(
            sql,
            log.getMateriaId(),
            log.getAcao().name(),
            log.getUsuario(),
            log.getDescricao()
        );
    }
}
