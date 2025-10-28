
package com.mocs_on.service;

import com.mocs_on.domain.Post;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.SpottedPost;

@Repository
public class SpottedDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<SpottedPost> recuperarTodos() {
        String sql = "SELECT autor, titulo, mensagem, data FROM avisos";
        
        return jdbcTemplate.query(sql, (resultado, linha) -> {
            SpottedPost spotted = new SpottedPost();
            
            spotted.setMensagem(resultado.getString("mensagem"));
            spotted.setNomeRemetente(resultado.getString("remetente"));
                      
            spotted.setStatus(Post.PostStatus.PUBLICO);
            Timestamp data = resultado.getTimestamp("data");
            spotted.setDataPublicacao(data.toLocalDateTime());
            return spotted;
        });
    }

    public int inserirAviso(SpottedPost spotted) {
        String sql = "INSERT INTO spotteds (mensagem, remetente, data, status) VALUES (?, ?, ?, ?)";

        Timestamp data = Timestamp.valueOf(spotted.getDataPublicacao());
        return jdbcTemplate.update(
            sql, 
            spotted.getMensagem(),
            spotted.getNomeRemetente(),
            data,
            spotted.getStatus() 
        );
    }

}

