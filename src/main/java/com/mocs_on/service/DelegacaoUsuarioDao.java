package com.mocs_on.service;

import com.mocs_on.dto.DelegadoDelegacaoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelegacaoUsuarioDao {

    private static final String LIST_DELEGADOS_SQL =
            "SELECT u.id AS usuario_id, u.nome, u.email, " +
            "ud.delegacao_id, d.nome AS delegacao_nome " +
            "FROM usuario_comite uc " +
            "JOIN usuarios u ON u.id = uc.usuario_id " +
            "LEFT JOIN usuario_delegacao ud ON ud.usuario_id = uc.usuario_id AND ud.comite_id = uc.comite_id " +
            "LEFT JOIN delegacoes d ON d.id = ud.delegacao_id " +
            "WHERE uc.comite_id = ? " +
            "AND UPPER(u.tipo) = 'DELEGADO' " +
            "ORDER BY u.nome";

    private static final String UPSERT_SQL =
            "INSERT INTO usuario_delegacao (usuario_id, comite_id, delegacao_id, updated_at) " +
            "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
            "ON DUPLICATE KEY UPDATE delegacao_id = VALUES(delegacao_id), updated_at = CURRENT_TIMESTAMP";

    public static List<DelegadoDelegacaoDTO> listarDelegadosPorComite(Connection conn, long comiteId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(LIST_DELEGADOS_SQL);
            ps.setLong(1, comiteId);
            rs = ps.executeQuery();
            List<DelegadoDelegacaoDTO> delegados = new ArrayList<>();
            while (rs.next()) {
                Long delegacaoId = rs.getObject("delegacao_id") == null ? null : rs.getLong("delegacao_id");
                DelegadoDelegacaoDTO dto = new DelegadoDelegacaoDTO(
                        rs.getLong("usuario_id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        delegacaoId,
                        rs.getString("delegacao_nome")
                );
                delegados.add(dto);
            }
            return delegados;
        } finally {
            close(ps, rs);
        }
    }

    public static void upsertDelegacoes(Connection conn, long comiteId, Map<Long, Long> assignments) throws SQLException {
        if (assignments == null || assignments.isEmpty()) {
            return;
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(UPSERT_SQL);
            for (Map.Entry<Long, Long> entry : assignments.entrySet()) {
                ps.setLong(1, entry.getKey());
                ps.setLong(2, comiteId);
                if (entry.getValue() == null) {
                    ps.setNull(3, Types.BIGINT);
                } else {
                    ps.setLong(3, entry.getValue());
                }
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            close(ps);
        }
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
