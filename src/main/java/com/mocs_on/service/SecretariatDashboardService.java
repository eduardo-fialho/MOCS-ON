package com.mocs_on.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class SecretariatDashboardService {

    @Autowired
    private AvisoDAO avisoService;
    @Autowired
    private DocumentoDAO documentoService;
    @Autowired
    private ComiteDao comiteService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DashboardMetrics collectMetrics() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        Long participants = queryForLong(sql);
        Long announcements = avisoService.quantidadeAvisos();
        Long documents = documentoService.quantidadeDocumentos();
        Long committees = comiteService.quantidadeComites();
        return new DashboardMetrics(participants, committees, documents, announcements);
    }

    private long queryForLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value != null ? value : 0L;
    }

    public record DashboardMetrics(Long totalParticipants,
                                   Long activeCommittees,
                                   Long documentsSent,
                                   Long announcementsPublished) {
    }
}
