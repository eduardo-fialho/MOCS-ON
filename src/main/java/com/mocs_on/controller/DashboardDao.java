package com.mocs_on.controller;
import java.sql.*;
public class DashboardDao extends AbstractDao{
    public static void init(Connection conn) throws SQLException{
        String sql="CREATE TABLE IF NOT EXISTS DashboardData(message VARCHAR(2000), file MEDIUMBLOB, posts JSON, date DATE)";
        Statement statement=conn.createStatement();
        statement.executeUpdate(sql);

    }
        public static String getMessage(Connection conn)throws SQLException{
            String sql="SELECT message FROM DashboardData ORDER BY date DESC LIMIT 1";
            Statement statement=conn.createStatement();
            ResultSet rs=statement.executeQuery(sql);
            return rs.getString(1);
    }
}