
package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.springframework.dao.DataAccessException;

import com.mocs_on.domain.Post;

@Repository
public class PostDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Post> recuperarTodos() {
        String sql = "SELECT id, autor, mensagem, data, status FROM posts ORDER BY data DESC";

        List<Post> posts = jdbcTemplate.query(sql, (resultado, linha) -> {
            Post post = new Post();
            post.setId(resultado.getLong("id"));
            post.setAutor(resultado.getString("autor"));
            post.setMensagem(resultado.getString("mensagem"));

            String statusStr = resultado.getString("status");
            if (statusStr != null) {
                try {
                    post.setStatus(Post.TipoPost.valueOf(statusStr));
                } catch (IllegalArgumentException ex) {
                    post.setStatus(Post.TipoPost.PUBLICO);
                }
            }

            Timestamp data = resultado.getTimestamp("data");
            if (data != null) post.setData(data.toLocalDateTime());
            return post;
        });

        String sqlReacoes = "SELECT emoji, COUNT(*) AS cnt FROM post_reactions WHERE post_id = ? GROUP BY emoji";
        for (Post p : posts) {
            Map<String, Integer> map = new HashMap<>();
            jdbcTemplate.query(sqlReacoes, new Object[]{p.getId()}, (rs) -> {
                String emoji = rs.getString("emoji");
                int cnt = rs.getInt("cnt");
                map.put(emoji, cnt);
            });
            p.setReactions(map);
        }

        return posts;
    }

    public Long inserirPost(Post post) {
        String sql = "INSERT INTO posts (autor, mensagem, data, status) VALUES (?, ?, ?, ?)";
        Timestamp ts = Timestamp.valueOf(post.getData());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, post.getAutor());
            ps.setString(2, post.getMensagem());
            ps.setTimestamp(3, ts);
            ps.setString(4, post.getStatus() == null ? Post.TipoPost.PUBLICO.name() : post.getStatus().name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            Long id = key.longValue();
            post.setId(id);
            return id;
        } else {
            return null;
        }
    }
    
    //reações por Usuario:
    
     public int addReactionToPost(Long postId, String usuario, String emoji) {
        String sql = "INSERT INTO post_reactions (post_id, usuario, emoji) VALUES (?, ?, ?)";
        try {
            return jdbcTemplate.update(sql, postId, usuario, emoji);
        } catch (DataAccessException ex) {
            return 0;
        }
    }

    public int removeReactionFromPost(Long postId, String usuario, String emoji) {
        String sql = "DELETE FROM post_reactions WHERE post_id = ? AND usuario = ? AND emoji = ?";
        return jdbcTemplate.update(sql, postId, usuario, emoji);
    }

    public Map<String, Integer> getReactionsForPost(Long postId) {
        String sql = "SELECT emoji, COUNT(*) AS cnt FROM post_reactions WHERE post_id = ? GROUP BY emoji";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, new Object[]{postId}, rs -> {
            map.put(rs.getString("emoji"), rs.getInt("cnt"));
        });
        return map;
    }
    
    //Implementei essa função aqui: "softDeletePost" para ao invés de deletar diretamente do banco
    //ela apaneas alterar o status do post no banco:
    
    public int softDeletePost(Long id) {
        String sql = "UPDATE posts SET status = 'EXCLUIDO' WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    public int deletePost(Long postId) {
        String sqlReacoes = "DELETE FROM post_reactions WHERE post_id = ?";
        jdbcTemplate.update(sqlReacoes, postId);

        String sqlPost = "DELETE FROM posts WHERE id = ?";
        return jdbcTemplate.update(sqlPost, postId);
    }
}
