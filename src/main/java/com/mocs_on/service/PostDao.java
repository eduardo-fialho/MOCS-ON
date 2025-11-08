package com.mocs_on.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Vector;

import com.mocs_on.model.Post;

public class PostDao extends DaoBase {
    private static final String DASHBOARD_POST_DATA = "dashboard_post_data";
    private static final String DASHBOARD_REACTION_DATA = "dashboard_post_reaction_data";
    private static final String SELECT_POSTS_BY_COMITE = "SELECT * FROM " + DASHBOARD_POST_DATA
            + "WHERE comite_id=";
    private static final String SELECT_REACTION = "SELECT * FROM " + DASHBOARD_REACTION_DATA + " WHERE message_id=";
    private static final String CREATE_DASHBOARD_POST_DATA = "CREATE TABLE IF NOT EXISTS " + DASHBOARD_POST_DATA
            + "(message VARCHAR(2000), author VARCHAR(50), date DATETIME, status VARCHAR(20), comite_id INTEGER, id INTEGER PRIMARY KEY AUTO_INCREMENT)";
    private static final String CREATE_DASHBOARD_REACTION_DATA = "CREATE TABLE IF NOT EXISTS " + DASHBOARD_REACTION_DATA
            + "(like INTEGER, coracao INTEGER, riso INTEGER, surpresa INTEGER, triste INTEGER, raiva INTEGER, message_id INTEGER, FOREIGN KEY (message id) REFERENCES "
            + DASHBOARD_POST_DATA + "(id) ON DELETE CASCADE)";
    private static final String DELETE_MESSAGE = "DELETE FROM " + DASHBOARD_POST_DATA + " WHERE id=";

    public static void init() throws SQLException {
        getConnection();
        Statement statement = conn.createStatement();
        statement.executeUpdate(CREATE_DASHBOARD_POST_DATA);
        statement = conn.createStatement();
        statement.executeUpdate(CREATE_DASHBOARD_REACTION_DATA);
        closeResource(statement);
    }

    public static Post[] getPostsByComite(int comiteId) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(SELECT_POSTS_BY_COMITE);
        Vector<Post> posts = new Vector<>();
        do {
            String mensagem = rs.getString(1);
            String remetente = rs.getString(2);
            Timestamp dataSql = rs.getTimestamp(3);
            LocalDateTime data = dataSql.toLocalDateTime();
            String status = rs.getString(4);
            int id = rs.getInt(5);
            statement = conn.createStatement();
            rs = statement.executeQuery(SELECT_REACTION + id);
            int like = rs.getInt(1);
            int coracao = rs.getInt(2);
            int riso = rs.getInt(3);
            int surpresa = rs.getInt(4);
            int triste = rs.getInt(5);
            int raiva = rs.getInt(6);
            Post post = new Post(mensagem, remetente, null, status, data, like, coracao, riso, surpresa, triste, raiva);
            posts.add(post);
        } while (rs.next());
        Post[] postsArray = (Post[]) posts.toArray();
        closeResource(statement, rs);
        return postsArray;
    }

    public static void setStatus(int id, String status) throws SQLException {
        String sql = "UPDATE " + DASHBOARD_REACTION_DATA + " SET status=" + status + " WHERE id=" + id;
        Statement statement = conn.createStatement();
        statement.executeUpdate(sql);
        closeResource(statement);
    }

    public static void setReacao(String tipo, int id, int quantidade) throws SQLException {
        String sql = "UPDATE " + DASHBOARD_REACTION_DATA + " SET " + tipo + "=" + tipo + "+" + quantidade + " WHERE id="
                + id;
        Statement statement = conn.createStatement();
        statement.executeUpdate(sql);
        closeResource(statement);
    }

    public static void deletePost(int id) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(DELETE_MESSAGE + id);
        closeResource(statement);
    }
}