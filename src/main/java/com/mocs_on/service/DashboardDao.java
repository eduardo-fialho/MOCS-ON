package com.mocs_on.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Vector;

import com.mocs_on.model.Post;
import com.mocs_on.model.Post.PostStatus;

public class DashboardDao extends AbstractDao {
    private static String selectMessage = "SELECT * FROM DashboardMessageData ORDER BY date DESC";
    private static String selectReaction = "SELECT * FROM DashboardMessageReactionData WHERE message_id=";

    public static void init(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS DashboardMessageData(message VARCHAR(2000), author VARCHAR(50), date DATETIME, status VARCHAR(20), id INTEGER PRIMARY KEY AUTO_INCREMENT)";
        Statement statement = conn.createStatement();
        statement.executeUpdate(sql);
        sql = "CREATE TABLE IF NOT EXISTS DashboardMessageReactionData(like INTEGER, coracao INTEGER, riso INTEGER, surpresa INTEGER, triste INTEGER, raiva INTEGER, message_id INTEGER, FOREIGN KEY (message id) REFERENCES DashboardMessageData(id) ON DELETE CASCADE)";
        statement = conn.createStatement();
        statement.executeUpdate(sql);
    }

    public static Post[] getPosts(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(selectMessage);
        Vector<Post> posts = new Vector<>();
        do {
            String mensagem = rs.getString(1);
            String remetente = rs.getString(2);
            String statusSql = rs.getString(4);
            Timestamp dataSql = rs.getTimestamp(3);
            LocalDateTime data = dataSql.toLocalDateTime();
            PostStatus status = PostStatus.valueOf(statusSql);
            int id = rs.getInt(5);
            statement = conn.createStatement();
            rs = statement.executeQuery(selectReaction + id);
            int like = rs.getInt(1);
            int coracao = rs.getInt(2);
            int riso = rs.getInt(3);
            int surpresa = rs.getInt(4);
            int triste = rs.getInt(5);
            int raiva = rs.getInt(6);
            Post.Reacoes reacoes = new Post.Reacoes(like, coracao, riso, surpresa, triste, raiva);
            Post post = new Post(mensagem, remetente, null, status, data, reacoes);
            posts.add(post);
        } while (rs.next());
        Post[] postsArray = (Post[]) posts.toArray();
        return postsArray;
    }

    public static void setStatus(int id){

    }

    public static void setReacao(enum Reacao tipo, int id, int quantidade){

    }
    public static void deletePost(int id){

    }
}