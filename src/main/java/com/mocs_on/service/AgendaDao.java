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
        String sql = "INSERT INTO agenda_diaria (titulo, descricao, data_evento, hora_evento) VALUES (?, ?, ?, ?)";
        jdbc.update(sql, a.getTitulo(), a.getDescricao(), a.getData_evento(), a.getHora_evento());
    }

    public List<AgendaDiaria> listarPorMes(int ano, int mes) {

        String sql = """
                    SELECT * FROM agenda_diaria
                    WHERE YEAR(data_evento) = ? AND MONTH(data_evento) = ?
                """;

        return jdbc.query(sql, (rs, rowNum) -> {
            AgendaDiaria ev = new AgendaDiaria(
                    rs.getString("titulo"),
                    rs.getString("descricao"),
                    rs.getString("data_evento"),
                    rs.getString("hora_evento"));
            ev.setId(rs.getLong("id"));
            return ev;
        }, ano, mes);
    }

}
