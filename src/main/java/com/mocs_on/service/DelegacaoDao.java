package com.mocs_on.service;

import com.mocs_on.domain.Delegacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DelegacaoDao {

    private static final String INSERT_SQL =
            "INSERT IGNORE INTO delegacoes (comite_id, nome) VALUES (?, ?)";
    private static final String LIST_BY_COMITE_SQL =
            "SELECT id, comite_id, nome FROM delegacoes WHERE comite_id = ? ORDER BY nome";
    private static final String DELETE_BY_IDS_SQL =
            "DELETE FROM delegacoes WHERE comite_id = ? AND id = ?";
    private static final String UPDATE_SQL =
            "UPDATE delegacoes SET nome = ? WHERE comite_id = ? AND id = ?";

    public static void insertAll(Connection conn, long comiteId, List<String> nomes) throws SQLException {
        List<String> normalized = normalizeDelegacoes(nomes);
        if (normalized.isEmpty()) {
            return;
        }

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(INSERT_SQL);
            for (String nome : normalized) {
                ps.setLong(1, comiteId);
                ps.setString(2, nome);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            close(ps);
        }
    }

    public static List<Delegacao> listByComite(Connection conn, long comiteId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(LIST_BY_COMITE_SQL);
            ps.setLong(1, comiteId);
            rs = ps.executeQuery();
            List<Delegacao> delegacoes = new ArrayList<>();
            while (rs.next()) {
                Delegacao delegacao = new Delegacao(
                        rs.getLong("id"),
                        rs.getLong("comite_id"),
                        rs.getString("nome")
                );
                delegacoes.add(delegacao);
            }
            return delegacoes;
        } finally {
            close(ps, rs);
        }
    }

    public static void deleteByIds(Connection conn, long comiteId, List<Long> delegacaoIds) throws SQLException {
        if (delegacaoIds == null || delegacaoIds.isEmpty()) {
            return;
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(DELETE_BY_IDS_SQL);
            for (Long id : delegacaoIds) {
                if (id == null) {
                    continue;
                }
                ps.setLong(1, comiteId);
                ps.setLong(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            close(ps);
        }
    }

    public static void updateNames(Connection conn, long comiteId, Map<Long, String> updates) throws SQLException {
        if (updates == null || updates.isEmpty()) {
            return;
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(UPDATE_SQL);
            for (Map.Entry<Long, String> entry : updates.entrySet()) {
                Long id = entry.getKey();
                String nome = entry.getValue();
                if (id == null || nome == null) {
                    continue;
                }
                String trimmed = nome.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                ps.setString(1, trimmed);
                ps.setLong(2, comiteId);
                ps.setLong(3, id);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            close(ps);
        }
    }

    private static List<String> normalizeDelegacoes(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) {
            return List.of();
        }
        Map<String, String> unique = new LinkedHashMap<>();
        for (String raw : nomes) {
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String key = trimmed.toLowerCase(Locale.ROOT);
            unique.putIfAbsent(key, trimmed);
        }
        return new ArrayList<>(unique.values());
    }

    private static void close(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            // ignore
        }
        close(ps);
    }
}
