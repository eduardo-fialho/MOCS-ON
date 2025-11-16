package com.mocs_on.service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import com.mocs_on.model.Post;

public class PostDao extends DaoBase {
    private static final String DASHBOARD_POST_DATA = "dashboard_post_data";
    private static final String SELECT_POSTS_BY_COMITE = "SELECT * FROM " + DASHBOARD_POST_DATA
            + " WHERE comite_id=?";
    private static final String CREATE_DASHBOARD_POST_DATA = "CREATE TABLE IF NOT EXISTS " + DASHBOARD_POST_DATA
            + "(message VARCHAR(2000), author VARCHAR(50), date DATETIME, status VARCHAR(20), curtir INTEGER, coracao INTEGER, riso INTEGER, surpresa INTEGER, triste INTEGER, raiva INTEGER, comite_id INTEGER, id INTEGER PRIMARY KEY AUTO_INCREMENT)";
    private static final String DELETE_MESSAGE = "DELETE FROM " + DASHBOARD_POST_DATA + " WHERE id=?";
    private static final String CREATE_POST = "INSERT INTO " + DASHBOARD_POST_DATA
            + "(mensagem, autor, data, status, curtida, coracao, riso, surpresa, triste, raiva, comite_id) VALUES (?, ?, ?, ?, 0, 0, 0, 0, 0, 0, ?)";

    public static void init() throws SQLException {
        getConnection();
        PreparedStatement statement = conn.prepareStatement(CREATE_DASHBOARD_POST_DATA);
        statement.executeUpdate();
        closeResource(statement);
    }

    public static ArrayList<Post> getPostsByComite(int comiteId) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(SELECT_POSTS_BY_COMITE);
        statement.setInt(1, comiteId);
        ResultSet rs=statement.executeQuery();
        ArrayList<Post> posts = new ArrayList<>();
        while(rs.next()) {
            String mensagem = rs.getString("mensagem");
            String remetente = rs.getString("autor");
            LocalDateTime data = rs.getTimestamp("data").toLocalDateTime();
            //LocalDateTime data = dataSql.toLocalDateTime();
            String status = rs.getString("status");
            int id = rs.getInt("id");
            int curtida=rs.getInt("curtida");
            int coracao=rs.getInt("coracao");
            int riso=rs.getInt("riso");
            int surpresa=rs.getInt("surpresa");
            int triste=rs.getInt("triste");
            int raiva=rs.getInt("raiva");
            Post post = new Post(mensagem, remetente, null, status, data, curtida, coracao, riso, surpresa, triste, raiva, id);
            posts.add(post);
        }

        closeResource(statement, rs);
        return posts;
    }

    public static void setStatus(int id, String status) throws SQLException {
        String sql="UPDATE "+DASHBOARD_POST_DATA+" SET status="+status+" WHERE id="+id;
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.executeUpdate(sql);
        closeResource(statement);
    }

    public static void setReacao(String tipo, int id, int quantidade) throws SQLException {
        String sql = "UPDATE "+DASHBOARD_POST_DATA+" SET "+tipo+"="+tipo+quantidade+" WHERE id="+id;
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.executeUpdate(sql);
        closeResource(statement);
    }

    public static void setMensagem(String mensagem, int id) throws SQLException {
        String sql = "UPDATE " + DASHBOARD_POST_DATA + " SET mensagem=" + mensagem + " WHERE id=" + id;
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.executeUpdate(sql);
        closeResource(statement);
    }

    public static void deletePost(int id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(DELETE_MESSAGE);
        statement.setInt(1, id);
        statement.executeUpdate();
        closeResource(statement);
    }

    public static void createPost(String mensagem, String autor, LocalDateTime tempo, String status, int comiteId) throws SQLException{
        PreparedStatement statement = conn.prepareStatement(CREATE_POST);
        statement.setString(1, mensagem);
        statement.setString(2, autor);
        statement.setTimestamp(3, Timestamp.valueOf(tempo.toString()));
        statement.setString(4, status);
        statement.setInt(5, comiteId);
        statement.executeUpdate();
        closeResource(statement);
    }
}