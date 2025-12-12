package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CurtidaDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Long addCurtida(Long postId, String usuario, String usuarioNome) {
        String sql = "INSERT INTO post_curtidas (post_id, usuario, usuario_nome, created_at) VALUES (?, ?, ?, ?)";
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        try {
            int updated = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setLong(1, postId);
                ps.setString(2, usuario);
                ps.setString(3, usuarioNome);
                ps.setTimestamp(4, ts);
                return ps;
            });
            if (updated <= 0) return null;
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            return id;
        } catch (Exception ex) {
            return null;
        }
    }

    public int removeCurtida(Long postId, String usuario) {
        String sql = "DELETE FROM post_curtidas WHERE post_id = ? AND usuario = ?";
        return jdbcTemplate.update(sql, postId, usuario);
    }

    public boolean hasCurtida(Long postId, String usuario) {
        String sql = "SELECT COUNT(*) FROM post_curtidas WHERE post_id = ? AND usuario = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId, usuario);
        return count != null && count > 0;
    }

    public int countCurtidas(Long postId) {
        String sql = "SELECT COUNT(*) FROM post_curtidas WHERE post_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, postId);
        return count == null ? 0 : count;
    }

    public List<Map<String, String>> getCurtidasForPost(Long postId) {
        String sql = "SELECT pc.usuario, pc.usuario_nome, u.nome FROM post_curtidas pc LEFT JOIN usuarios u ON pc.usuario = u.email WHERE pc.post_id = ? ORDER BY pc.created_at ASC";
        List<Map<String, String>> out = new ArrayList<>();
        jdbcTemplate.query(sql, ps -> ps.setLong(1, postId), rs -> {
            Map<String, String> m = new HashMap<>();
            m.put("usuario", rs.getString("usuario"));
            String nome = rs.getString("usuario_nome");
            if (nome == null || nome.isBlank()) nome = rs.getString("nome");
            m.put("usuarioNome", nome != null ? nome : rs.getString("usuario"));
            out.add(m);
        });
        return out;
    }
}
