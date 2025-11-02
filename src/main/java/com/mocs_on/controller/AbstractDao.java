package com.mocs_on.controller;
import java.sql.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component; 
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
