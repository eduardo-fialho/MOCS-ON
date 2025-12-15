package com.mocs_on.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mocs_on.domain.GuiaEstudos;

public class GuiaEstudosDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<GuiaEstudos> recuperarTodos() {
        return null;
    }
}