package com.mocs_on.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SecretariadoDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> listarEmails() {
        String sql = "SELECT email FROM usuarios WHERE tipo = 'SECRETARIADO' AND email IS NOT NULL";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("email"));
    }
}
