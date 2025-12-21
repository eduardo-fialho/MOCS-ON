package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.mocs_on.domain.Comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CommentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private volatile Boolean legacySchema;
    private volatile Boolean statusColumn;
    private volatile Boolean usuarioNomeColumn;

    public List<Comment> getCommentsForPost(Long postId, int limit, int offset) {
        if (isLegacySchema()) {
            String sql = "SELECT id, post_id, autor, mensagem, created_at FROM post_comments WHERE post_id = ? ORDER BY created_at ASC LIMIT ? OFFSET ?";
            return jdbcTemplate.query(sql, new Object[]{postId, limit, offset}, (rs, rowNum) -> {
                Comment c = new Comment();
                c.setId(rs.getLong("id"));
                c.setPostId(rs.getLong("post_id"));
                String autor = rs.getString("autor");
                c.setUsuario(autor);
                c.setUsuarioNome(autor);
                c.setMensagem(rs.getString("mensagem"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                c.setStatus(null);
                return c;
            });
        }

        boolean hasUsuarioNome = hasUsuarioNomeColumn();
        boolean hasStatus = hasStatusColumn();
        String sql = "SELECT id, post_id, usuario"
                + (hasUsuarioNome ? ", usuario_nome" : "")
                + ", mensagem, created_at"
                + (hasStatus ? ", status" : "")
                + " FROM post_comments WHERE post_id = ? ORDER BY created_at ASC LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, new Object[]{postId, limit, offset}, (rs, rowNum) -> {
            Comment c = new Comment();
            c.setId(rs.getLong("id"));
            c.setPostId(rs.getLong("post_id"));
            c.setUsuario(rs.getString("usuario"));
            if (hasUsuarioNome) {
                c.setUsuarioNome(rs.getString("usuario_nome"));
            }
            c.setMensagem(rs.getString("mensagem"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
            if (hasStatus) {
                c.setStatus(rs.getString("status"));
            }
            return c;
        });
    }

    public List<Comment> getCommentsForPost(Long postId) {
        return getCommentsForPost(postId, 200, 0);
    }

    public Long addCommentToPost(Long postId, String usuario, String usuarioNome, String mensagem) {
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (isLegacySchema()) {
            String sql = "INSERT INTO post_comments (post_id, autor, mensagem, created_at) VALUES (?, ?, ?, ?)";
            String autor = (usuarioNome != null && !usuarioNome.isBlank()) ? usuarioNome : usuario;
            int updated = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setLong(1, postId);
                ps.setString(2, autor);
                ps.setString(3, mensagem);
                ps.setTimestamp(4, ts);
                return ps;
            }, keyHolder);
            if (updated <= 0) return null;
            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;
        }

        boolean hasUsuarioNome = hasUsuarioNomeColumn();
        boolean hasStatus = hasStatusColumn();
        String sql = "INSERT INTO post_comments (post_id, usuario"
                + (hasUsuarioNome ? ", usuario_nome" : "")
                + ", mensagem, created_at"
                + (hasStatus ? ", status" : "")
                + ") VALUES (?, ?"
                + (hasUsuarioNome ? ", ?" : "")
                + ", ?, ?"
                + (hasStatus ? ", ?" : "")
                + ")";

        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
            int idx = 1;
            ps.setLong(idx++, postId);
            ps.setString(idx++, usuario);
            if (hasUsuarioNome) {
                ps.setString(idx++, usuarioNome);
            }
            ps.setString(idx++, mensagem);
            ps.setTimestamp(idx++, ts);
            if (hasStatus) {
                ps.setString(idx++, null);
            }
            return ps;
        }, keyHolder);

        if (updated <= 0) return null;
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public Comment findById(Long commentId) {
        try {
            if (isLegacySchema()) {
                String sql = "SELECT id, post_id, autor, mensagem, created_at FROM post_comments WHERE id = ?";
                return jdbcTemplate.queryForObject(sql, new Object[]{commentId}, (rs, rowNum) -> {
                    Comment c = new Comment();
                    c.setId(rs.getLong("id"));
                    c.setPostId(rs.getLong("post_id"));
                    String autor = rs.getString("autor");
                    c.setUsuario(autor);
                    c.setUsuarioNome(autor);
                    c.setMensagem(rs.getString("mensagem"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                    c.setStatus(null);
                    return c;
                });
            }

            boolean hasUsuarioNome = hasUsuarioNomeColumn();
            boolean hasStatus = hasStatusColumn();
            String sql = "SELECT id, post_id, usuario"
                    + (hasUsuarioNome ? ", usuario_nome" : "")
                    + ", mensagem, created_at"
                    + (hasStatus ? ", status" : "")
                    + " FROM post_comments WHERE id = ?";

            return jdbcTemplate.queryForObject(sql, new Object[]{commentId}, (rs, rowNum) -> {
                Comment c = new Comment();
                c.setId(rs.getLong("id"));
                c.setPostId(rs.getLong("post_id"));
                c.setUsuario(rs.getString("usuario"));
                if (hasUsuarioNome) {
                    c.setUsuarioNome(rs.getString("usuario_nome"));
                }
                c.setMensagem(rs.getString("mensagem"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                if (hasStatus) {
                    c.setStatus(rs.getString("status"));
                }
                return c;
            });
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public int softDeleteComment(Long commentId) {
        if (!hasStatusColumn()) {
            String sql = "DELETE FROM post_comments WHERE id = ?";
            return jdbcTemplate.update(sql, commentId);
        }
        String sql = "UPDATE post_comments SET status = 'EXCLUIDO' WHERE id = ?";
        return jdbcTemplate.update(sql, commentId);
    }

    private boolean isLegacySchema() {
        Boolean cached = legacySchema;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (legacySchema == null) {
                legacySchema = !hasColumn("usuario");
            }
            return legacySchema;
        }
    }

    private boolean hasStatusColumn() {
        Boolean cached = statusColumn;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (statusColumn == null) {
                statusColumn = hasColumn("status");
            }
            return statusColumn;
        }
    }

    private boolean hasUsuarioNomeColumn() {
        Boolean cached = usuarioNomeColumn;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (usuarioNomeColumn == null) {
                usuarioNomeColumn = hasColumn("usuario_nome");
            }
            return usuarioNomeColumn;
        }
    }

    private boolean hasColumn(String column) {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'post_comments' AND column_name = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, column);
            return count != null && count > 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
