package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import com.mocs_on.domain.Post;
import com.mocs_on.domain.Usuario;
import org.springframework.beans.factory.annotation.Qualifier;
import com.mocs_on.service.LoginDAO;

@Repository
public class PostDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private LoginDAO loginDAO;

    public enum ReactionResult {
        CREATED, UPDATED, REMOVED, ERROR
    }

    public List<Post> recuperarTodos() {
        String sql = "SELECT id, autor, mensagem, data, status, comite_sigla, aprovador FROM posts ORDER BY data DESC";

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
            post.setComiteSigla(resultado.getString("comite_sigla"));
            post.setAprovador(resultado.getString("aprovador"));
            return post;
        });

        String sqlReacoes = "SELECT emoji, COUNT(*) AS cnt FROM post_reactions WHERE post_id = ? GROUP BY emoji";
        for (Post p : posts) {
            Map<String, Integer> map = new HashMap<>();
            jdbcTemplate.query(sqlReacoes, ps -> ps.setLong(1, p.getId()), rs -> {
                map.put(rs.getString("emoji"), rs.getInt("cnt"));
            });
            p.setReactions(map);
        }

        return posts;
    }

    public List<Post> recuperarTodosParaUsuario(String usuario) {
        String sql = "SELECT id, autor, mensagem, data, status, comite_sigla, aprovador FROM posts ORDER BY data DESC";

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
            post.setComiteSigla(resultado.getString("comite_sigla"));
            post.setAprovador(resultado.getString("aprovador"));
            return post;
        });

        String sqlReacoes = "SELECT emoji, COUNT(*) AS cnt FROM post_reactions WHERE post_id = ? GROUP BY emoji";
        String sqlUserReaction = "SELECT emoji FROM post_reactions WHERE post_id = ? AND usuario = ?";

        for (Post p : posts) {
            Map<String, Integer> map = new HashMap<>();
            jdbcTemplate.query(sqlReacoes, ps -> ps.setLong(1, p.getId()), rs -> {
                map.put(rs.getString("emoji"), rs.getInt("cnt"));
            });
            p.setReactions(map);

            if (usuario != null && !usuario.trim().isEmpty()) {
                try {
                    String my = jdbcTemplate.queryForObject(sqlUserReaction, new Object[]{p.getId(), usuario}, String.class);
                    p.setMyReaction(my);
                } catch (EmptyResultDataAccessException ex) {
                    p.setMyReaction(null);
                }
            } else {
                p.setMyReaction(null);
            }
        }

        return posts;
    }

    public Long inserirPost(Post post) {
        String sql = "INSERT INTO posts (autor, mensagem, data, status, comite_sigla, aprovador) VALUES (?, ?, ?, ?, ?, ?)";
        Timestamp ts = Timestamp.valueOf(post.getData());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, post.getAutor());
            ps.setString(2, post.getMensagem());
            ps.setTimestamp(3, ts);
            ps.setString(4, post.getStatus() == null ? Post.TipoPost.PUBLICO.name() : post.getStatus().name());
            ps.setString(5, post.getComiteSigla());
            ps.setString(6, post.getAprovador());
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

    
    public int addReactionToPost(Long postId, String usuario, String emoji) {
        String sql = "INSERT INTO post_reactions (post_id, usuario, emoji) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql, postId, usuario, emoji);
    }

    
    public int removeReactionFromPost(Long postId, String usuario, String emoji) {
        String sql = "DELETE FROM post_reactions WHERE post_id = ? AND usuario = ? AND emoji = ?";
        return jdbcTemplate.update(sql, postId, usuario, emoji);
    }

    public int removeReactionByUser(Long postId, String usuario) {
        String sql = "DELETE FROM post_reactions WHERE post_id = ? AND usuario = ?";
        return jdbcTemplate.update(sql, postId, usuario);
    }

    public Map<String, Integer> getReactionsForPost(Long postId) {
        String sql = "SELECT emoji, COUNT(*) AS cnt FROM post_reactions WHERE post_id = ? GROUP BY emoji";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, ps -> ps.setLong(1, postId), rs -> {
            map.put(rs.getString("emoji"), rs.getInt("cnt"));
        });
        return map;
    }

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

    public String getUserReactionForPost(Long postId, String usuario) {
        String sql = "SELECT emoji FROM post_reactions WHERE post_id = ? AND usuario = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{postId, usuario}, String.class);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public Post getPostById(Long postId) {
        String sql = "SELECT id, autor, mensagem, data, status, comite_sigla, aprovador FROM posts WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{postId}, (rs, rowNum) -> {
                Post post = new Post();
                post.setId(rs.getLong("id"));
                post.setAutor(rs.getString("autor"));
                post.setMensagem(rs.getString("mensagem"));
                String statusStr = rs.getString("status");
                if (statusStr != null) {
                    try {
                        post.setStatus(Post.TipoPost.valueOf(statusStr));
                    } catch (IllegalArgumentException ex) {
                        post.setStatus(Post.TipoPost.PUBLICO);
                    }
                }
                java.sql.Timestamp ts = rs.getTimestamp("data");
                if (ts != null) post.setData(ts.toLocalDateTime());
                post.setComiteSigla(rs.getString("comite_sigla"));
                post.setAprovador(rs.getString("aprovador"));
                return post;
            });
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    public int updateReactionForPost(Long postId, String usuario, String newEmoji) {
        String sql = "UPDATE post_reactions SET emoji = ? WHERE post_id = ? AND usuario = ?";
        return jdbcTemplate.update(sql, newEmoji, postId, usuario);
    }


    @Transactional
    public ReactionResult reactToPost(Long postId, String usuario, String emoji) {
        if (postId == null || usuario == null || usuario.trim().isEmpty() || emoji == null) {
            return ReactionResult.ERROR;
        }

        // Enforce rules for Consulta Informal
        Post post = getPostById(postId);
        if (post == null) return ReactionResult.ERROR;
        if (post.getStatus() == Post.TipoPost.CONSULTA_INFORMAL) {
            // normalize emoji
            String norm = emoji.trim().toUpperCase();
            if (norm.equals("NÃO") || norm.equals("NÃO")) norm = "NAO";
            if (!norm.equals("SIM") && !norm.equals("NAO") && !norm.equals("NÃO")) {
                return ReactionResult.ERROR;
            }

            // validate user is delegado and belongs to committee
            var userOpt = loginDAO.findByEmail(usuario);
            if (userOpt.isEmpty()) return ReactionResult.ERROR;
            Usuario u = userOpt.get();
            if (u.getTipo() == null || !u.getTipo().name().equalsIgnoreCase("DELEGADO")) {
                return ReactionResult.ERROR;
            }

            boolean belongs = false;
            if (u.getComites() != null && post.getComiteSigla() != null) {
                for (var c : u.getComites()) {
                    if (c != null && post.getComiteSigla().equalsIgnoreCase(c.getSigla())) {
                        belongs = true; break;
                    }
                }
            }
            if (!belongs) return ReactionResult.ERROR;

            // map emoji to normalized values SIM/NAO
            emoji = norm.equals("NÃO") ? "NAO" : norm;
        }

        String current = getUserReactionForPost(postId, usuario);

        try {
            if (current == null) {
                
                try {
                    int inserted = addReactionToPost(postId, usuario, emoji);
                    if (inserted > 0) return ReactionResult.CREATED;
                    return ReactionResult.ERROR;
                } catch (DuplicateKeyException dkex) {
                    
                    current = getUserReactionForPost(postId, usuario);
                    if (current == null) return ReactionResult.ERROR;

                    if (current.equals(emoji)) {
                        int deleted = removeReactionFromPost(postId, usuario, emoji);
                        return deleted > 0 ? ReactionResult.REMOVED : ReactionResult.ERROR;
                    } else {
                        int updated = updateReactionForPost(postId, usuario, emoji);
                        return updated > 0 ? ReactionResult.UPDATED : ReactionResult.ERROR;
                    }
                }
            }

            
            if (current.equals(emoji)) {
                
                int removed = removeReactionByUser(postId, usuario);
                return removed > 0 ? ReactionResult.REMOVED : ReactionResult.ERROR;
            } else {
                
                int updated = updateReactionForPost(postId, usuario, emoji);
                if (updated > 0) return ReactionResult.UPDATED;

                removeReactionByUser(postId, usuario);
                int inserted = addReactionToPost(postId, usuario, emoji);
                return inserted > 0 ? ReactionResult.UPDATED : ReactionResult.ERROR;
            }
        } catch (DataAccessException ex) {
            return ReactionResult.ERROR;
        }
    }
}
