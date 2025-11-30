package com.mocs_on.service;

import com.mocs_on.domain.AgendaDiaria;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AgendaService {

    private final AgendaDao dao;

    public AgendaService(AgendaDao dao) {
        this.dao = dao;
    }

    public void salvar(AgendaDiaria agenda) {
        dao.salvar(agenda);
    }

    public List<AgendaDiaria> listarPorMes(int ano, int mes) {
        return dao.listarPorMes(ano, mes);
    }

    public List<AgendaDiaria> listarTodos() {
        return dao.listarTodos();
    }

    public void editar(Long id, AgendaDiaria dados) {
        dao.editar(id, dados);
    }

    public void atualizarVisibilidade(Long id, boolean visivel) {
        dao.atualizarVisibilidade(id, visivel);
    }
}

