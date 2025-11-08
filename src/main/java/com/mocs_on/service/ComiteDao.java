package com.mocs_on.service;

import java.sql.*;
import com.mocs_on.model.Comite;
import java.util.Vector;

public class ComiteDao extends DaoBase {
    private static final String SELECT_COMITES = "SELECT * FROM comites ORDER BY nome";
    private static final String DELETE_COMITE = "DELETE FROM comites WHERE id=";
    private static final String SELECT_COMITE = "SELECT * FROM COMITES WHERE id=";
    private static final String SET_COMITE = "INSERT INTO comites(nome, sigla, status, num_delegados) VALUES(";
    private static final String CREATE_COMITE = "CREATE TABLE IF NOT EXISTS comites(nome VARCHAR(50), sigla VARCHAR(10), status VARCHAR(20), num_delegados INTEGER, id INTEGER PRIMARY KEY AUTO_INCREMENT)";
    private static Connection conn;

    public static void init() throws SQLException {
        try {
            getConnection();
        } catch (SQLException e) {
            // a fazer
        }
        Statement statement = conn.createStatement();
        statement.executeUpdate(CREATE_COMITE);
        closeResource(statement);
    }

    public static Comite[] getComites() throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(SELECT_COMITES);
        Vector<Comite> comites = new Vector<>();
        do {

            String nome = rs.getString(1);
            String sigla = rs.getString(2);
            String status = rs.getString(3);
            int numDelegados = rs.getInt(4);
            Comite comite = new Comite(nome, sigla, status, numDelegados);
            comites.add(comite);
        } while (rs.next());
        Comite[] comitesArray = (Comite[]) comites.toArray();
        closeResource(statement, rs);
        return comitesArray;
    }

    public static void setComite(Comite comite) throws SQLException {
        String nome = comite.getNome();
        String sigla = comite.getSigla();
        String status = comite.getStatus();
        int numDelegados = comite.getNumDelegados();
        String sql = SET_COMITE + nome + ", " + sigla + ", " + status + ", " + numDelegados + ")";
        Statement statement = conn.createStatement();
        statement.executeUpdate(sql);
        closeResource(statement);
    }

    public static Comite getComite(int id) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(SELECT_COMITE + id);
        String nome = rs.getString(1);
        String sigla = rs.getString(2);
        String status = rs.getString(3);
        int numDelegados = rs.getInt(4);
        Comite comite=new Comite(nome, sigla, status, numDelegados);
        closeResource(statement, rs);
        return comite;
    }

    public void deleteComite(int id) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(DELETE_COMITE + id);
        closeResource(statement);
    }
}
