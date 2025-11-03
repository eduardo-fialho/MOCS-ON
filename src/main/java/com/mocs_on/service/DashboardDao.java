package com.mocs_on.service;
import java.time.LocalDateTime;
import java.sql.*;
import com.mocs_on.domain.Post;
import com.mocs_on.domain.Post.PostStatus;
public class DashboardDao extends AbstractDao {
    public static void init(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS DashboardData(message VARCHAR(2000), author VARCHAR(50), date DATETIME, status VARCHAR(20))";
        Statement statement = conn.createStatement();
        statement.executeUpdate(sql);

    }

    public static String getMessage(Connection conn) throws SQLException {
        String sql = "SELECT message FROM DashboardData ORDER BY date DESC LIMIT 1";
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        return rs.getString(1);
    }
    public static Post[] getPosts(Connection conn) throws SQLException{
        String sql="SELECT * FROM DashboardData ORDER BY date DESC";
        int row=0;
        Statement statement=conn.createStatement();
        ResultSet rs=statement.executeQuery(sql);
        if(rs.last()){
            row=rs.getRow();
            rs.first();
        }else{
            return null;
        }
        Post[] posts=new Post[row];
        for(int i=0;i<row;i++){
            String mensagem=rs.getString(1);
            String remetente=rs.getString(2);
            String statusSql=rs.getString(4);
            Timestamp dataSql=rs.getTimestamp(3);
            LocalDateTime data=dataSql.toLocalDateTime();

            posts[i]=new Post(mensagem, remetente, null, PostStatus.valueOf(statusSql),data);
            rs.next();
        }
        return posts;
    }
}