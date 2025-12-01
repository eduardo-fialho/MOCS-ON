package com.mocs_on.service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.beans.factory.annotation.Value;
public class AbstractDao {
    @Value("${spring.datasource.url")
    static String url;
    @Value("${spring.datasource.username")
    static String user;
    @Value("${spring.datasource.password")
    static String pwd;
    public static Connection getConnection() throws SQLException{    
        Connection conn=DriverManager.getConnection(url, user, pwd);
        conn.setAutoCommit(false);
        return conn;
    }

    protected static void closeResource( Statement statement, ResultSet rs){
        try {
            rs.close();
        } catch (Exception e) {
            rs=null;
        }
        try {
            statement.close();
        } catch (Exception e) {
        statement=null;
        }
    } 
    
    protected static void closeResource(Statement statement){
        try {
            statement.close();
        } catch (Exception e) {
            statement=null;
        }
    }
}
