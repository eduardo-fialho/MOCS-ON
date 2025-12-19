package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.mocs_on.domain.Comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.mocs_on.service.PostDAO postDAO;

    public List<Comment> getCommentsForPost(Long postId, int limit, int offset) {
        String sql = "SELECT id, post_id, usuario, usuario_nome, mensagem, created_at, status FROM post_comments WHERE post_id = ? ORDER BY created_at ASC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{postId, limit, offset}, (rs, rowNum) -> {
            Comment c = new Comment();
            c.setId(rs.getLong("id"));
            c.setPostId(rs.getLong("post_id"));
            c.setUsuario(rs.getString("usuario"));
            c.setUsuarioNome(rs.getString("usuario_nome"));
            c.setMensagem(rs.getString("mensagem"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
            c.setStatus(rs.getString("status"));
            return c;
        });
    }

    public List<Comment> getCommentsForPost(Long postId) {
        return getCommentsForPost(postId, 200, 0);
    }

    public Long addCommentToPost(Long postId, String usuario, String usuarioNome, String mensagem) {
        // Prevent comments on Consulta Informal posts
        com.mocs_on.domain.Post post = postDAO.getPostById(postId);
        if (post != null && post.getStatus() == com.mocs_on.domain.Post.TipoPost.CONSULTA_INFORMAL) {
            return null; // disallow comments
        }
        String sql = "INSERT INTO post_comments (post_id, usuario, usuario_nome, mensagem, created_at, status) VALUES (?, ?, ?, ?, ?, ?)";
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            ps.setLong(1, postId);
            ps.setString(2, usuario);
            ps.setString(3, usuarioNome);
            ps.setString(4, mensagem);
            ps.setTimestamp(5, ts);
            ps.setString(6, null);
            return ps;
        });
        if (updated <= 0) return null;
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return id;
    }

    public Comment findById(Long commentId) {
        String sql = "SELECT id, post_id, usuario, usuario_nome, mensagem, created_at, status FROM post_comments WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{commentId}, (rs, rowNum) -> {
                Comment c = new Comment();
                c.setId(rs.getLong("id"));
                c.setPostId(rs.getLong("post_id"));
                c.setUsuario(rs.getString("usuario"));
                c.setUsuarioNome(rs.getString("usuario_nome"));
                c.setMensagem(rs.getString("mensagem"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                c.setStatus(rs.getString("status"));
                return c;
            });
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public int softDeleteComment(Long commentId) {
        String sql = "UPDATE post_comments SET status = 'EXCLUIDO' WHERE id = ?";
        return jdbcTemplate.update(sql, commentId);
    }
}
