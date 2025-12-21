package com.mocs_on.service;

import com.mocs_on.dto.ComiteResumoDTO;
import com.mocs_on.dto.ComiteDelegacaoDTO;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class UsuarioComiteDao {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioComiteDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ComiteResumoDTO> listarComites() {
        String sql = "SELECT id, nome, sigla FROM comites ORDER BY nome";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ComiteResumoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("sigla")
        ));
    }

    public List<ComiteResumoDTO> listarComitesPorUsuario(long usuarioId) {
        String sql = """
                SELECT c.id, c.nome, c.sigla
                FROM usuario_comite uc
                JOIN comites c ON c.id = uc.comite_id
                WHERE uc.usuario_id = ?
                ORDER BY c.nome
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ComiteResumoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("sigla")
        ), usuarioId);
    }

    public List<ComiteDelegacaoDTO> listarComitesComDelegacao(long usuarioId) {
        String sql = """
                SELECT c.id, c.nome, c.sigla, d.nome AS delegacao_nome
                FROM usuario_comite uc
                JOIN comites c ON c.id = uc.comite_id
                LEFT JOIN usuario_delegacao ud ON ud.usuario_id = uc.usuario_id AND ud.comite_id = uc.comite_id
                LEFT JOIN delegacoes d ON d.id = ud.delegacao_id
                WHERE uc.usuario_id = ?
                ORDER BY c.nome
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ComiteDelegacaoDTO(
                rs.getLong("id"),
                rs.getString("nome"),
                rs.getString("sigla"),
                rs.getString("delegacao_nome")
        ), usuarioId);
    }

    public List<Long> listarComiteIds(long usuarioId) {
        String sql = "SELECT comite_id FROM usuario_comite WHERE usuario_id = ? ORDER BY comite_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("comite_id"), usuarioId);
    }

    public void substituirComites(long usuarioId, List<Long> comiteIds) {
        jdbcTemplate.update("DELETE FROM usuario_comite WHERE usuario_id = ?", usuarioId);

        List<Long> normalized = normalizeComiteIds(comiteIds);
        cleanupDelegacoes(usuarioId, normalized);
        if (normalized.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO usuario_comite (usuario_id, comite_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, usuarioId);
                ps.setLong(2, normalized.get(i));
            }

            @Override
            public int getBatchSize() {
                return normalized.size();
            }
        });
    }

    private void cleanupDelegacoes(long usuarioId, List<Long> comiteIds) {
        if (comiteIds == null || comiteIds.isEmpty()) {
            jdbcTemplate.update("DELETE FROM usuario_delegacao WHERE usuario_id = ?", usuarioId);
            return;
        }
        String placeholders = String.join(",", comiteIds.stream().map(id -> "?").toList());
        String sql = "DELETE FROM usuario_delegacao WHERE usuario_id = ? AND comite_id NOT IN (" + placeholders + ")";
        List<Object> params = new ArrayList<>();
        params.add(usuarioId);
        params.addAll(comiteIds);
        jdbcTemplate.update(sql, params.toArray());
    }

    private List<Long> normalizeComiteIds(List<Long> comiteIds) {
        if (comiteIds == null) {
            return List.of();
        }
        return comiteIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0)
                .distinct()
                .toList();
    }
}
