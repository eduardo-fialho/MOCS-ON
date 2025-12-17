package com.mocs_on.service;

import com.mocs_on.domain.AgendaDiaria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaService {

    private final AgendaDao dao;

    public AgendaService(AgendaDao dao) {
        this.dao = dao;
    }

    public void salvar(AgendaDiaria agenda) {
        dao.salvar(agenda);
    }

    public List<AgendaDiaria> listarPorMes(int ano, int mes, String tipo) {
        return dao.listarPorMes(ano, mes, tipo);
    }

    public List<AgendaDiaria> listarTodos(String tipo) {
        return dao.listarTodos(tipo);
    }

    public void editar(Long id, AgendaDiaria dados) {
        dao.editar(id, dados);
    }

    public void atualizarVisibilidade(Long id, boolean visivel) {
        dao.atualizarVisibilidade(id, visivel);
    }
}
