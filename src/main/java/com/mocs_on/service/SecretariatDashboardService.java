package com.mocs_on.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Aggregates the KPI values displayed on the Secretariado dashboard.
 */
@Service
public class SecretariatDashboardService {

    private static final String PARTICIPANTS_SQL = """
            SELECT COUNT(*) FROM usuarios
            """;

    private static final String ANNOUNCEMENTS_SQL = """
            SELECT COUNT(*) FROM avisos
            """;

    /**
     * Gallery posts (stored with the PHOTO| prefix) are the closest representation
     * of uploads/documents currently available in the application.
     */
    private static final String DOCUMENTS_SQL = """
            SELECT COUNT(*) FROM posts
            WHERE mensagem LIKE 'PHOTO|%'
            """;

    /**
     * Distinct committee names provided either by user profiles (delegados já
     * aprovados) or pré-inscrições. It's our proxy for committees that estão ativos
     * ou em formação.
     */
    private static final String ACTIVE_COMMITTEES_SQL = """
            SELECT COUNT(*) FROM (
                SELECT DISTINCT TRIM(UPPER(comite_preferido)) AS nome
                FROM user_profiles
                WHERE comite_preferido IS NOT NULL AND comite_preferido <> ''

                UNION

                SELECT DISTINCT TRIM(UPPER(comite_preferido)) AS nome
                FROM pre_registrations
                WHERE comite_preferido IS NOT NULL AND comite_preferido <> ''
            ) committees
            """;

    private final JdbcTemplate jdbcTemplate;

    public SecretariatDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DashboardMetrics collectMetrics() {
        long participants = queryForLong(PARTICIPANTS_SQL);
        long announcements = queryForLong(ANNOUNCEMENTS_SQL);
        long documents = queryForLong(DOCUMENTS_SQL);
        long committees = queryForLong(ACTIVE_COMMITTEES_SQL);
        return new DashboardMetrics(participants, committees, documents, announcements);
    }

    private long queryForLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value != null ? value : 0L;
    }

    public record DashboardMetrics(long totalParticipants,
                                   long activeCommittees,
                                   long documentsSent,
                                   long announcementsPublished) {
    }
}
