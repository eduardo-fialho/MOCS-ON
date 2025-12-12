package com.mocs_on.service;

import com.mocs_on.domain.PreRegistration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PreRegistrationDAO {

    private final JdbcTemplate jdbcTemplate;
    private final BeanPropertyRowMapper<PreRegistration> mapper = new BeanPropertyRowMapper<>(PreRegistration.class);

    public PreRegistrationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insert(PreRegistration registration) {
        String sql = """
                INSERT INTO pre_registrations
                    (nome, email, instituicao, telefone, comite_preferido, mensagem, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, 'PENDENTE', ?)
                """;
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql,
                registration.getNome(),
                registration.getEmail(),
                registration.getInstituicao(),
                registration.getTelefone(),
                registration.getComitePreferido(),
                registration.getMensagem(),
                Timestamp.valueOf(now));
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (id != null) {
            registration.setId(id);
        }
        registration.setStatus(PreRegistration.Status.PENDENTE);
        registration.setCreatedAt(now);
        return id == null ? 0L : id;
    }

    public List<PreRegistration> findPending() {
        String sql = """
                SELECT id, nome, email, instituicao, telefone, comite_preferido, mensagem, status,
                       created_at, processed_at, processed_by
                FROM pre_registrations
                WHERE status = 'PENDENTE'
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(sql, mapper);
    }

    public int countPending() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pre_registrations WHERE status = 'PENDENTE'", Integer.class);
        return count == null ? 0 : count;
    }

    public Optional<PreRegistration> findById(long id) {
        String sql = """
                SELECT id, nome, email, instituicao, telefone, comite_preferido, mensagem, status,
                       created_at, processed_at, processed_by
                FROM pre_registrations WHERE id = ?
                """;
        List<PreRegistration> list = jdbcTemplate.query(sql, mapper, id);
        return list.stream().findFirst();
    }

    public void markProcessed(long id, String processedBy) {
        jdbcTemplate.update("""
                UPDATE pre_registrations
                SET status = 'PROCESSADO',
                    processed_at = ?,
                    processed_by = ?
                WHERE id = ? AND status = 'PENDENTE'
                """, Timestamp.valueOf(LocalDateTime.now()), processedBy, id);
    }

    public boolean markDenied(long id, String processedBy) {
        int updated = jdbcTemplate.update("""
                UPDATE pre_registrations
                SET status = 'NEGADO',
                    processed_at = ?,
                    processed_by = ?
                WHERE id = ? AND status = 'PENDENTE'
                """, Timestamp.valueOf(LocalDateTime.now()), processedBy, id);
        return updated > 0;
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM pre_registrations WHERE id = ?", id);
    }
}
