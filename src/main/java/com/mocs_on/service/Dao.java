package com.mocs_on.service;

import java.sql.*;

public abstract class Dao{
    public Connection getConnection() throws SQLException{
        HDataSource ds = new HDataSource();
        ds.init();
        return ds.getConnection();
    }
}