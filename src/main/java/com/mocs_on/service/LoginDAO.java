package com.mocs_on.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Login;

@Repository
public class LoginDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean validateUser() {
    }

}
