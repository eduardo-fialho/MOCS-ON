package com.mocs_on.service;

import com.mocs_on.domain.AgendaDiaria;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AgendaDao {

    private final JdbcTemplate jdbc;

    public AgendaDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void salvar(AgendaDiaria a) {
        String sql = """
                    INSERT INTO agenda_diaria
                    (titulo, descricao, data_evento, hora_evento, visivel, tipo)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbc.update(
                sql,
                a.getTitulo(),
                a.getDescricao(),
                a.getData_evento(),
                a.getHora_evento(),
                a.getVisivel(),
                a.getTipo());
    }

    public List<AgendaDiaria> listarPorMes(int ano, int mes, String tipo) {
        String sql = """
                    SELECT * FROM agenda_diaria
                    WHERE YEAR(data_evento) = ?
                      AND MONTH(data_evento) = ?
                      AND visivel = true
                      AND tipo = ?
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            AgendaDiaria ev = new AgendaDiaria(
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getString("data_evento"),
                    rs.getString("hora_evento"),
                    rs.getBoolean("visivel"),
                    rs.getString("tipo"));
            ev.setId(rs.getLong("id"));
            return ev;
        }, ano, mes, tipo);
    }

    public List<AgendaDiaria> listarTodos(String tipo) {
        String sql = """
                    SELECT * FROM agenda_diaria
                    WHERE visivel = true
                      AND tipo = ?
                    ORDER BY data_evento, hora_evento
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            AgendaDiaria ev = new AgendaDiaria(
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getString("data_evento"),
                    rs.getString("hora_evento"),
                    rs.getBoolean("visivel"),
                    rs.getString("tipo"));
            ev.setId(rs.getLong("id"));
            return ev;
        }, tipo);
    }

    public void editar(Long id, AgendaDiaria e) {
        String sql = """
                    UPDATE agenda_diaria
                    SET titulo = ?, descricao = ?, data_evento = ?, hora_evento = ?, tipo = ?
                    WHERE id = ?
                """;

        jdbc.update(
                sql,
                e.getTitulo(),
                e.getDescricao(),
                e.getData_evento(),
                e.getHora_evento(),
                e.getTipo(),
                id);
    }

    public void atualizarVisibilidade(Long id, boolean visivel) {
        String sql = "UPDATE agenda_diaria SET visivel = ? WHERE id = ?";
        jdbc.update(sql, visivel, id);
    }
}
