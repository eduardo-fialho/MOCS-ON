package com.mocs_on.service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Consulta;
import com.mocs_on.domain.StatusConsulta;

@Repository
public class ConsultaDAO {

    private final JdbcTemplate jdbcTemplate;

    private final org.springframework.jdbc.core.RowMapper<Consulta> mapper = (rs, rowNum) -> {
        Consulta c = new Consulta();
        c.setId(rs.getLong("id"));
        c.setTitulo(rs.getString("titulo"));
        c.setPergunta(rs.getString("pergunta"));
        c.setStatus(StatusConsulta.valueOf(rs.getString("status")));
        c.setAtivo(rs.getBoolean("ativo"));

        Timestamp criacao = rs.getTimestamp("created_at");
        Timestamp aprovacao = rs.getTimestamp("approved_at");

        if (criacao != null) c.setCreatedAt(criacao.toLocalDateTime());
        if (aprovacao != null) c.setApprovedAt(aprovacao.toLocalDateTime());

        return c;
    };

    public ConsultaDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long salvar(Consulta consulta) {
        String sql = """
            INSERT INTO consulta
            (titulo, pergunta, status, ativo)
            VALUES (?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, consulta.getTitulo());
            ps.setString(2, consulta.getPergunta());
            ps.setString(3, consulta.getStatus().name());
            ps.setBoolean(4, consulta.isAtivo());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        consulta.setId(id);
        return id;
    }

    public Consulta buscarPorId(Long id) {
        String sql = "SELECT * FROM consulta WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Consulta c = new Consulta();
            c.setId(rs.getLong("id"));
            c.setTitulo(rs.getString("titulo"));
            c.setPergunta(rs.getString("pergunta"));
            c.setStatus(StatusConsulta.valueOf(rs.getString("status")));
            c.setAtivo(rs.getBoolean("ativo"));

            Timestamp criacao = rs.getTimestamp("created_at");
            Timestamp aprovacao = rs.getTimestamp("approved_at");

            if (criacao != null) c.setCreatedAt(criacao.toLocalDateTime());
            if (aprovacao != null) c.setApprovedAt(aprovacao.toLocalDateTime());

            return c;
        }, id);
    }

    public List<Consulta> listarTodas() {
        String sql = """
            SELECT *
            FROM consulta
            ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, mapper);
    }

    public List<Consulta> listarPorStatus(StatusConsulta status) {
        String sql = """
            SELECT *
            FROM consulta
            WHERE status = ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Consulta c = new Consulta();
            c.setId(rs.getLong("id"));
            c.setTitulo(rs.getString("titulo"));
            c.setPergunta(rs.getString("pergunta"));
            c.setStatus(StatusConsulta.valueOf(rs.getString("status")));
            c.setAtivo(rs.getBoolean("ativo"));
            return c;
        }, status.name());
    }

    public List<Consulta> listarAtivas() {
        String sql = """
            SELECT *
            FROM consulta
            WHERE ativo = true
            ORDER BY created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Consulta c = new Consulta();
            c.setId(rs.getLong("id"));
            c.setTitulo(rs.getString("titulo"));
            c.setPergunta(rs.getString("pergunta"));
            c.setStatus(StatusConsulta.valueOf(rs.getString("status")));
            c.setAtivo(rs.getBoolean("ativo"));
            return c;
        });
    }

    public void atualizar(Consulta consulta) {
        String sql = """
            UPDATE consulta
            SET titulo = ?, pergunta = ?
            WHERE id = ?
        """;

        jdbcTemplate.update(
            sql,
            consulta.getTitulo(),
            consulta.getPergunta(),
            consulta.getId()
        );
    }

    public void atualizarStatus(Long id, StatusConsulta status) {
        String sql = """
            UPDATE consulta
            SET status = ?, approved_at = NOW()
            WHERE id = ?
        """;

        jdbcTemplate.update(sql, status.name(), id);
    }

    public void atualizarAtivo(Long id, boolean ativo) {
        String sql = """
            UPDATE consulta
            SET ativo = ?
            WHERE id = ?
        """;

        jdbcTemplate.update(sql, ativo, id);
    }

    public int arquivar(Long id) {
        String sql = """
            UPDATE consulta
            SET ativo = false
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, id);
    }
}
