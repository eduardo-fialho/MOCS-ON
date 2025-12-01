package com.mocs_on.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.mocs_on.domain.Post.TipoPost;
import com.mocs_on.domain.Comite;
import com.mocs_on.domain.Comite.StatusComite;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import com.mocs_on.domain.Post;

@Repository
public class PostDAO extends AbstractDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
public List<Post> recuperarPorComite(Long comiteId) {
    String sql = "SELECT id, autor, mensagem, data, status, comite_id " +
                 "FROM posts WHERE comite_id = ?";

    return jdbcTemplate.query(sql, new Object[]{comiteId}, (rs, rowNum) -> {
        Post p = new Post();
        p.setId(rs.getLong("id"));
        p.setAutor(rs.getString("autor"));
        p.setMensagem(rs.getString("mensagem"));

        Timestamp ts = rs.getTimestamp("data");
        p.setData(ts.toLocalDateTime());
        String statusStr = rs.getString("status");
        p.setStatus(TipoPost.valueOf(statusStr));
        return p;
    });
}
        

    public enum ReactionResult {
        CREATED, UPDATED, REMOVED, ERROR
    }

    public List<Post> recuperarTodos() {
        String sql = "SELECT id, autor, mensagem, data, status, comite_id FROM posts ORDER BY data DESC";

        List<Post> posts = jdbcTemplate.query(sql, (resultado, linha) -> {
            Post post = new Post();
            post.setId(resultado.getLong("id"));
            post.setAutor(resultado.getString("autor"));
            post.setMensagem(resultado.getString("mensagem"));
            post.setComiteId(resultado.getLong("comite_id"));

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
            jdbcTemplate.query(sqlReacoes, ps -> ps.setLong(1, p.getId()), rs -> {
                map.put(rs.getString("emoji"), rs.getInt("cnt"));
            });
            p.setReactions(map);
        }

        return posts;
    }

    public List<Post> recuperarTodosParaUsuario(String usuario) {
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
        String sql = "INSERT INTO posts (autor, mensagem, data, status, comite_id) VALUES (?, ?, ?, ?, ?)";
        Timestamp ts = Timestamp.valueOf(post.getData());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, post.getAutor());
            ps.setString(2, post.getMensagem());
            ps.setTimestamp(3, ts);
            ps.setString(4, post.getStatus() == null ? Post.TipoPost.PUBLICO.name() : post.getStatus().name());
            ps.setLong(5, post.getComiteId());
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

    public ArrayList<Post> getByStatus(Connection conn, TipoPost status) throws Exception, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String getsql = "SELECT * FROM posts WHERE status = ?";

        try {
            ps = conn.prepareStatement(getsql);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return new ArrayList<Post>();
            }
            ArrayList<Post> list = new ArrayList<Post>();
            do {
                String nome = rs.getString("autor");
                String mensagem = rs.getString("mensagem");
                TipoPost status1 = TipoPost.valueOf(rs.getString("status"));
                Timestamp dataTs = rs.getTimestamp("data");
                LocalDateTime data = dataTs.toLocalDateTime();
                
                
                Post post = new Post(mensagem, nome, status1, data);
                
                list.add(post);

            } 
            while (rs.next());
            
            return list;
        } catch (SQLException e) {
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
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

    public Post set(ResultSet rs) throws SQLException {
        Post vo = new Post();
        vo.setId(rs.getLong("id"));
        vo.setAutor(rs.getString("autor"));
        vo.setMensagem(rs.getString("mensagem"));
        vo.setData(rs.getTimestamp("data").toLocalDateTime());
        vo.setStatus(Post.TipoPost.valueOf(rs.getString("status")));
        
        return vo;
    }

    public Post get(Connection conn, long id) throws Exception, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        String getsql = "SELECT * FROM posts WHERE id = ? ";

        try {
            ps = conn.prepareStatement(getsql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new Exception("Object not found [" + id + "]");
            }
            Post b = set(rs);
            return b;
        } catch (SQLException e) {
            throw e;
        } finally {
            closeResource(ps, rs);
            ps = null;
            rs = null;
        }
    }

    public void update(Connection conn, Post vo) throws Exception, SQLException {
        PreparedStatement ps = null;
        
        String updatesql = "UPDATE posts SET autor = ?, mensagem = ?, status = ?, data = ? WHERE id = ?";

        try {
            ps = conn.prepareStatement(updatesql);
            ps.setString(1, vo.getAutor());
            ps.setString(2, vo.getMensagem());
            ps.setString(3, vo.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(vo.getData()));
            ps.setLong(5, vo.getId());
            int count = ps.executeUpdate();
            if (count == 0) {
                throw new Exception("Object not found [" + vo.getId() + "] .");
            }
            // SEM COMMIT
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception e1) {
            }
            ;
            throw e;
        } finally {
            closeResource(ps);
            ps = null;
    public String getUserReactionForPost(Long postId, String usuario) {
        String sql = "SELECT emoji FROM post_reactions WHERE post_id = ? AND usuario = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{postId, usuario}, String.class);
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
