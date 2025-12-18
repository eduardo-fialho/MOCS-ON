package com.mocs_on.service;

import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Materia;
import com.mocs_on.domain.StatusMateria;

@Repository
public class MateriaDAO {

    private final JdbcTemplate jdbcTemplate;

    public MateriaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long salvar(Materia materia) {
        String sql = """
            INSERT INTO materias
            (titulo, lead, texto, autor, comite_id, status, ativo, imagem)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, materia.getTitulo());
            ps.setString(2, materia.getLead());
            ps.setString(3, materia.getTexto());
            ps.setString(4, materia.getAutor());
            ps.setObject(5, materia.getComiteId());
            ps.setString(6, materia.getStatus().name());
            ps.setBoolean(7, materia.isAtivo());
            ps.setBytes(8, materia.getImagem());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        materia.setId(id);
        return id;
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

            Timestamp criacao = rs.getTimestamp("data_criacao");
            Timestamp edicao = rs.getTimestamp("data_edicao");
            Timestamp aprovacao = rs.getTimestamp("data_aprovacao");

            if (criacao != null) m.setCreatedAt(criacao.toLocalDateTime());
            if (edicao != null) m.setUpdatedAt(edicao.toLocalDateTime());
            if (aprovacao != null) m.setReviewedAt(aprovacao.toLocalDateTime());

            return m;
        }, id);
    }

    public List<Materia> listar() {
        String sql = "SELECT * FROM materias ORDER BY data_criacao DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Materia m = new Materia();
            m.setId(rs.getLong("id"));
            m.setTitulo(rs.getString("titulo"));
            m.setLead(rs.getString("lead"));
            m.setStatus(StatusMateria.valueOf(rs.getString("status")));
            m.setAutor(rs.getString("autor"));
            m.setAtivo(rs.getBoolean("ativo"));

            Timestamp criacao = rs.getTimestamp("data_criacao");
            if (criacao != null) {
                m.setCreatedAt(criacao.toLocalDateTime());
            }

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
            SET titulo = ?, lead = ?, texto = ?, imagem = ?, ativo = ?, data_edicao = NOW()
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
            SET status = ?, data_aprovacao = NOW()
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, status.name(), id);
    }

    public int atualizarRevisor(Long id, String revisor) {
        String sql = """
            UPDATE materias
            SET revisor = ?
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, revisor, id);
    }

    public List<Materia> listarAprovadas() {
        String sql = """
            SELECT *
            FROM materias
            WHERE status = 'APROVADA'
            AND ativo = true
            ORDER BY data_criacao DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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

            Timestamp criacao = rs.getTimestamp("data_criacao");
            Timestamp edicao = rs.getTimestamp("data_edicao");
            Timestamp aprovacao = rs.getTimestamp("data_aprovacao");

            if (criacao != null) m.setCreatedAt(criacao.toLocalDateTime());
            if (edicao != null) m.setUpdatedAt(edicao.toLocalDateTime());
            if (aprovacao != null) m.setReviewedAt(aprovacao.toLocalDateTime());

            return m;
        });
    }
}
