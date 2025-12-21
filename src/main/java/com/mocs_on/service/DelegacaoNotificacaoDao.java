package com.mocs_on.service;

import com.mocs_on.dto.DelegacaoNotificacaoDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DelegacaoNotificacaoDao {

    private static final String INSERT_SQL =
            "INSERT INTO delegacao_notificacoes (usuario_id, comite_id, mensagem) VALUES (?, ?, ?)";

    public static void insertAll(Connection conn, List<DelegacaoNotificacaoDTO> notificacoes) throws SQLException {
        if (notificacoes == null || notificacoes.isEmpty()) {
            return;
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(INSERT_SQL);
            for (DelegacaoNotificacaoDTO notificacao : notificacoes) {
                ps.setLong(1, notificacao.getUsuarioId());
                ps.setLong(2, notificacao.getComiteId());
                ps.setString(3, notificacao.getMensagem());
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
