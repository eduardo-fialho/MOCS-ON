package com.mocs_on.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Materia;
import com.mocs_on.domain.StatusMateria;

@Repository
public class MateriaDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int inserir(Materia materia) {
        String sql = """
            INSERT INTO materias
            (titulo, lead, imagem, texto, autor, status, comite_id, ativo, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())
        """;

        return jdbcTemplate.update(
            sql,
            materia.getTitulo(),
            materia.getLead(),
            materia.getImagem(),
            materia.getTexto(),
            materia.getAutor(),
            materia.getStatus().name(),
            materia.getComiteId(),
            true
        );
    }

    public int atualizar(Materia materia) {
        String sql = """
            UPDATE materias
            SET titulo = ?, lead = ?, imagem = ?, texto = ?, data_edicao = NOW()
            WHERE id = ?
        """;

        return jdbcTemplate.update(
            sql,
            materia.getTitulo(),
            materia.getLead(),
            materia.getImagem(),
            materia.getTexto(),
            materia.getId()
        );
    }

    public int avaliar(Long id, StatusMateria status, String revisor) {
        String sql = """
            UPDATE materias
            SET status = ?, revisor = ?
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, status.name(), revisor, id);
    }

    public Materia buscarPorId(Long id) {
        String sql = "SELECT * FROM materias WHERE id = ? AND ativo = true";

        List<Materia> lista = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Materia m = new Materia();
            m.setId(rs.getLong("id"));
            m.setTitulo(rs.getString("titulo"));
            m.setLead(rs.getString("lead"));
            m.setImagem(rs.getBytes("imagem"));
            m.setTexto(rs.getString("texto"));
            m.setAutor(rs.getString("autor"));
            m.setRevisor(rs.getString("revisor"));
            m.setComiteId(rs.getObject("comite_id", Long.class));
            m.setAtivo(rs.getBoolean("ativo"));
            m.setStatus(StatusMateria.valueOf(rs.getString("status")));

            Timestamp dc = rs.getTimestamp("data_criacao");
            Timestamp de = rs.getTimestamp("data_edicao");

            m.setDataCriacao(dc != null ? dc.toLocalDateTime() : null);
            m.setDataEdicao(de != null ? de.toLocalDateTime() : null);

            return m;
        }, id);

        return lista.stream().findFirst().orElse(null);
    }

    public List<Materia> listarPendentes() {
        String sql = """
            SELECT * FROM materias
            WHERE status = 'PENDENTE' AND ativo = true
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Materia m = new Materia();
            m.setId(rs.getLong("id"));
            m.setTitulo(rs.getString("titulo"));
            m.setAutor(rs.getString("autor"));
            m.setStatus(StatusMateria.PENDENTE);
            return m;
        });
    }
}
