package com.mocs_on.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Aviso;

@Repository
public class AvisoDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Aviso> recuperarTodos() {
        String sql = "SELECT autor, titulo, mensagem, data FROM avisos";
        
        return jdbcTemplate.query(sql, (resultado, linha) -> {
            Aviso aviso = new Aviso();
            aviso.setAutor(resultado.getString("autor"));
            aviso.setTitulo(resultado.getString("titulo"));
            aviso.setMensagem(resultado.getString("mensagem"));
            Timestamp data = resultado.getTimestamp("data");
            aviso.setData(data.toLocalDateTime());
            return aviso;
        });
    }

    public int inserirAviso(Aviso aviso) {
        String sql = "INSERT INTO avisos (autor, titulo, mensagem, data) VALUES (?, ?, ?, ?)";

        Timestamp data = Timestamp.valueOf(aviso.getData());
        return jdbcTemplate.update(
            sql, 
            aviso.getAutor(), 
            aviso.getTitulo(), 
            aviso.getMensagem(), 
            data
        );
    }

    public int quantidadeAvisos() {
        String sql = "SELECT COUNT(*) FROM avisos";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

}
