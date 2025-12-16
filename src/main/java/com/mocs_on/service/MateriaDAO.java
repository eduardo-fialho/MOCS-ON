package com.mocs_on.service;

import com.mocs_on.domain.Materia;
import com.mocs_on.domain.StatusMateria;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class MateriaDAO {

    private final JdbcTemplate jdbcTemplate;

    public MateriaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int salvar(Materia materia) {
        String sql = """
            INSERT INTO materias
            (titulo, lead, texto, autor, comite_id, status, ativo, imagem, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
        """;

        return jdbcTemplate.update(
            sql,
            materia.getTitulo(),
            materia.getLead(),
            materia.getTexto(),
            materia.getAutor(),
            materia.getComiteId(),
            materia.getStatus().name(),
            materia.isAtivo(),
            materia.getImagem()
        );
    }

    public Materia buscarPorId(Long id) {
        String sql = "SELECT * FROM materias WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Materia m = new Materia();
            m.setId(rs.getLong("id"));
            m.setTitulo(rs.getString("titulo"));
            m.setLead(rs.getString("lead"));
            m.setTexto(rs.getString("texto"));
            m.setImagem(rs.getBytes("imagem"));
            m.setAutor(rs.getString("autor"));
            m.setRevisor(rs.getString("revisor"));
            m.setComiteId(rs.getObject("comite_id", Long.class));
            m.setStatus(StatusMateria.valueOf(rs.getString("status")));
            m.setAtivo(rs.getBoolean("ativo"));

            Timestamp created = rs.getTimestamp("created_at");
            Timestamp updated = rs.getTimestamp("updated_at");
            Timestamp reviewed = rs.getTimestamp("reviewed_at");

            if (created != null) m.setCreatedAt(created.toLocalDateTime());
            if (updated != null) m.setUpdatedAt(updated.toLocalDateTime());
            if (reviewed != null) m.setReviewedAt(reviewed.toLocalDateTime());

            return m;
        }, id);
    }

    public List<Materia> listar() {
        String sql = "SELECT * FROM materias ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Materia m = new Materia();
            m.setId(rs.getLong("id"));
            m.setTitulo(rs.getString("titulo"));
            m.setLead(rs.getString("lead"));
            m.setStatus(StatusMateria.valueOf(rs.getString("status")));
            m.setAutor(rs.getString("autor"));
            m.setAtivo(rs.getBoolean("ativo"));

            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) m.setCreatedAt(created.toLocalDateTime());

            return m;
        });
    }

    public List<Materia> listarPendentes() {
        String sql = "SELECT * FROM materias WHERE status = 'PENDENTE'";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Materia m = new Materia();
            m.setId(rs.getLong("id"));
            m.setTitulo(rs.getString("titulo"));
            m.setLead(rs.getString("lead"));
            m.setStatus(StatusMateria.PENDENTE);
            m.setAutor(rs.getString("autor"));
            return m;
        });
    }

    public int atualizar(Materia materia) {
        String sql = """
            UPDATE materias
            SET titulo = ?, lead = ?, texto = ?, imagem = ?, ativo = ?, updated_at = NOW()
            WHERE id = ?
        """;

        return jdbcTemplate.update(
            sql,
            materia.getTitulo(),
            materia.getLead(),
            materia.getTexto(),
            materia.getImagem(),
            materia.isAtivo(),
            materia.getId()
        );
    }

    public int atualizarStatus(Long id, StatusMateria status) {
        String sql = """
            UPDATE materias
            SET status = ?, reviewed_at = NOW()
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, status.name(), id);
    }
}
