package com.mocs_on.service;

import com.mocs_on.domain.AgendaDiaria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgendaDao {

    @Autowired
    private final JdbcTemplate jdbc;

    public AgendaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void salvar(AgendaDiaria a) {
        String sql = "INSERT INTO agenda_diaria (titulo, descricao, data_evento, hora_evento, visivel) VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql, a.getTitulo(), a.getDescricao(), a.getData_evento(), a.getHora_evento(), a.getVisivel());
    }

    public List<AgendaDiaria> listarPorMes(int ano, int mes) {

        String sql = """
                    SELECT * FROM agenda_diaria
                    WHERE YEAR(data_evento) = ? 
                        AND MONTH(data_evento) = ?
                        AND visivel = true
                    """;

        return jdbc.query(sql, (rs, rowNum) -> {
            AgendaDiaria ev = new AgendaDiaria(
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getString("data_evento"),
                    rs.getString("hora_evento"),
                    rs.getBoolean("visivel"));
            ev.setId(rs.getLong("id"));
            return ev;
        }, ano, mes);
    }

    public List<AgendaDiaria> listarTodos() {
        String sql = """
                SELECT * FROM agenda_diaria 
                WHERE visivel = true
            """;
                    
        return jdbc.query(sql, (rs, rowNum) -> {
            AgendaDiaria ev = new AgendaDiaria(
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getString("data_evento"),
                    rs.getString("hora_evento"),
                    rs.getBoolean("visivel"));
            ev.setId(rs.getLong("id"));
            return ev;
        });
    }

    public void editar(Long id, AgendaDiaria e) {
        String sql = """
                UPDATE agenda_diaria 
                SET titulo = ?, descricao = ?, data_evento = ?, hora_evento = ?
                WHERE id = ?
                """;
        jdbc.update(sql, e.getTitulo(), e.getDescricao(), e.getData_evento(), e.getHora_evento(), id);
    }

    public void atualizarVisibilidade(Long id, boolean visivel) {
        String sql = "UPDATE agenda_diaria SET visivel = ? WHERE id = ?";
        jdbc.update(sql, visivel, id);
    }
}
