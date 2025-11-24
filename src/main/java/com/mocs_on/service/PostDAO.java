package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.springframework.dao.DataAccessException;

import com.mocs_on.domain.Post;
import com.mocs_on.domain.PostComment;

@Repository
public class PostDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Post> recuperarTodos() {
        String sql = "SELECT id, autor, mensagem, data, status FROM posts " +
                "WHERE mensagem IS NULL OR mensagem NOT LIKE 'PHOTO|%' " +
                "ORDER BY data DESC";

        List<Post> posts = mapPosts(sql);
        populateReactions(posts);
        return posts;
    }

    public List<Post> recuperarGaleria() {
        String sql = "SELECT id, autor, mensagem, data, status FROM posts " +
                "WHERE mensagem LIKE 'PHOTO|%' " +
                "ORDER BY data DESC";
        List<Post> posts = mapPosts(sql);
        populateReactions(posts);
        return posts;
    }

    public Optional<Post> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, autor, mensagem, data, status FROM posts WHERE id = ?";
        List<Post> result = mapPosts(sql, id);
        return result.stream().findFirst();
    }

    public Optional<Post> findLatestVisiblePostByAuthor(String author) {
        if (author == null || author.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT id, autor, mensagem, data, status FROM posts " +
                "WHERE autor = ? AND status <> 'EXCLUIDO' ORDER BY data DESC LIMIT 1";

        List<Post> result = jdbcTemplate.query(sql, (resultado, linha) -> {
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
            if (data != null) {
                post.setData(data.toLocalDateTime());
            }
            return post;
        }, author);

        return result.stream().findFirst();
    }

    /** Ajuste solicitado pelo usuário: separar feed de texto e fotos para a curadoria (#galeria). */
    private List<Post> mapPosts(String sql, Object... args) {
        return jdbcTemplate.query(sql, (resultado, linha) -> {
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
        }, args);
    }

    private void populateReactions(List<Post> posts) {
        String sqlReacoes = "SELECT emoji, COUNT(*) AS cnt FROM post_reactions WHERE post_id = ? GROUP BY emoji";
        for (Post p : posts) {
            Map<String, Integer> map = new HashMap<>();
            jdbcTemplate.query(sqlReacoes, ps -> ps.setLong(1, p.getId()), rs -> {
                map.put(rs.getString("emoji"), rs.getInt("cnt"));
            });
            p.setReactions(map);
        }
    }

    public List<PostComment> listComments(Long postId) {
        String sql = "SELECT id, post_id, autor, mensagem, created_at FROM post_comments " +
                "WHERE post_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PostComment comment = new PostComment();
            comment.setId(rs.getLong("id"));
            comment.setPostId(rs.getLong("post_id"));
            comment.setAutor(rs.getString("autor"));
            comment.setMensagem(rs.getString("mensagem"));
            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) {
                comment.setCreatedAt(created.toLocalDateTime());
            }
            return comment;
        }, postId);
    }

    public int addComment(Long postId, String autor, String mensagem) {
        String sql = "INSERT INTO post_comments (post_id, autor, mensagem, created_at) VALUES (?, ?, ?, ?)";
        Timestamp now = Timestamp.valueOf(java.time.LocalDateTime.now());
        return jdbcTemplate.update(sql, postId, autor, mensagem, now);
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

    /** Remove todas as reações (likes) de todos os posts. */
    public int deleteAllReactions() {
        return jdbcTemplate.update("DELETE FROM post_reactions");
    }
}
