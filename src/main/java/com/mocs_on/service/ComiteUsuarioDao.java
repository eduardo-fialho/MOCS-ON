package com.mocs_on.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ComiteUsuarioDao {

    private static final String INSERT_SQL =
            "INSERT IGNORE INTO usuario_comite (usuario_id, comite_id) VALUES (?, ?)";

    public static void substituirUsuariosNoComite(Connection conn, long comiteId, List<Long> usuarioIds)
            throws SQLException {
        List<Long> normalized = normalizeIds(usuarioIds);
        if (normalized.isEmpty()) {
            deleteAll(conn, comiteId);
            deleteDelegacoes(conn, comiteId, null);
            return;
        }

        deleteNotIn(conn, comiteId, normalized);
        deleteDelegacoes(conn, comiteId, normalized);
        insertMissing(conn, comiteId, normalized);
    }

    private static void deleteAll(Connection conn, long comiteId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM usuario_comite WHERE comite_id = ?")) {
            ps.setLong(1, comiteId);
            ps.executeUpdate();
        }
    }

    private static void deleteNotIn(Connection conn, long comiteId, List<Long> usuarioIds) throws SQLException {
        String placeholders = String.join(",", usuarioIds.stream().map(id -> "?").toList());
        String sql = "DELETE FROM usuario_comite WHERE comite_id = ? AND usuario_id NOT IN (" + placeholders + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, comiteId);
            int index = 2;
            for (Long id : usuarioIds) {
                ps.setLong(index++, id);
            }
            ps.executeUpdate();
        }
    }

    private static void deleteDelegacoes(Connection conn, long comiteId, List<Long> usuarioIds) throws SQLException {
        String sql;
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            sql = "DELETE FROM usuario_delegacao WHERE comite_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, comiteId);
                ps.executeUpdate();
            }
            return;
        }
        String placeholders = String.join(",", usuarioIds.stream().map(id -> "?").toList());
        sql = "DELETE FROM usuario_delegacao WHERE comite_id = ? AND usuario_id NOT IN (" + placeholders + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, comiteId);
            int index = 2;
            for (Long id : usuarioIds) {
                ps.setLong(index++, id);
            }
            ps.executeUpdate();
        }
    }

    private static void insertMissing(Connection conn, long comiteId, List<Long> usuarioIds) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            for (Long usuarioId : usuarioIds) {
                ps.setLong(1, usuarioId);
                ps.setLong(2, comiteId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> unique = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null && id > 0) {
                unique.add(id);
            }
        }
        return new ArrayList<>(unique);
    }
}
