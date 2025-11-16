package com.mocs_on.service;

import java.sql.*;
import com.mocs_on.model.Comite;
import java.util.ArrayList;

public class ComiteDao extends DaoBase {
    private static final String SELECT_COMITES = "SELECT * FROM comites ORDER BY nome";
    private static final String DELETE_COMITE = "DELETE FROM comites WHERE id=?";
    private static final String SELECT_COMITE = "SELECT * FROM COMITES WHERE id=?";
    private static final String SET_COMITE = "INSERT INTO comites(nome, sigla, status, num_delegados) VALUES(?, ?, ?, ?)";
    private static final String CREATE_COMITE = "CREATE TABLE IF NOT EXISTS comites(nome VARCHAR(50), sigla VARCHAR(10), status VARCHAR(20), num_delegados INTEGER, id INTEGER PRIMARY KEY AUTO_INCREMENT)";

    public static void init() throws SQLException {
        getConnection();
        PreparedStatement statement = conn.prepareStatement(CREATE_COMITE);
        statement.executeUpdate();
        closeResource(statement);
    }

    public static ArrayList<Comite> getComites() throws SQLException {
        
        PreparedStatement statement = conn.prepareStatement(SELECT_COMITES);
        ResultSet rs = statement.executeQuery();
        ArrayList<Comite> comites = new ArrayList<>();
        while(rs.next()) {

            String nome = rs.getString("nome");
            String sigla = rs.getString("sigla");
            String status = rs.getString("status");
            int numDelegados = rs.getInt("num_delegados");
            int id=rs.getInt("id");
            Comite comite = new Comite(sigla, nome, status, numDelegados, id);
            comites.add(comite);
        }
        closeResource(statement, rs);
        return comites;
    }

    public static void setComite(Comite comite) throws SQLException {
        String nome = comite.getNome();
        String sigla = comite.getSigla();
        String status = comite.getStatus();
        int numDelegados = comite.getNumDelegados();
        PreparedStatement statement = conn.prepareStatement(SET_COMITE);
        statement.setString(1, nome);
        statement.setString(2, sigla);
        statement.setString(3, status);
        statement.setInt(4, numDelegados);
        statement.executeUpdate();
        closeResource(statement);
    }

    public static Comite getComite(int id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(SELECT_COMITE);
        statement.setInt(1, id);
        ResultSet rs=statement.executeQuery();
        String nome = rs.getString("nome");
        String sigla = rs.getString("sigla");
        String status = rs.getString("status");
        int numDelegados = rs.getInt("num_delegados");
        Comite comite=new Comite(nome, sigla, status, numDelegados);
        closeResource(statement, rs);
        return comite;
    }

    public void deleteComite(int id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(DELETE_COMITE);
        statement.setInt(1, id);
        statement.executeUpdate();
        closeResource(statement);
    }
}
