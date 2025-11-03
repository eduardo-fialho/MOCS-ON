package com.mocs_on.service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.Statement;
public class AbstractDao {

    public static Connection getConnection() throws SQLException{    
        String url="jdbc:mysql://localhost:3306/MOCSON?createDatabaseIfNotExist=true&characterEncoding=UTF-8";
        String pwd="";
        String user="root";
        //altere conforme os dados do usuario
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
