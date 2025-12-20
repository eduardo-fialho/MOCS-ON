package com.mocs_on.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mocs_on.domain.ListaPresenca;
import com.mocs_on.domain.RegistroPresenca;
import com.mocs_on.dto.ComiteResumoDTO;
import com.mocs_on.dto.PresencaDetalheDTO;

@Service
public class PresencaService {

    private final PresencaDao dao;

    public PresencaService(PresencaDao dao) {
        this.dao = dao;
    }

    public List<ComiteResumoDTO> listarComites() {
        return dao.listarComites();
    }

    public List<ListaPresenca> listarListas() {
        return dao.listarListas();
    }

    public PresencaDetalheDTO obterDetalhe(long id) {
        ListaPresenca lista = dao.buscarLista(id);
        if (lista == null) {
            return null;
        }
        List<RegistroPresenca> registros = dao.listarRegistros(id);
        return new PresencaDetalheDTO(lista, registros);
    }

    public ListaPresenca criarLista(ListaPresenca lista) {
        long id = dao.inserirLista(lista);
        lista.setId(id);
        List<RegistroPresenca> delegados = dao.listarDelegados();
        if (!delegados.isEmpty()) {
            dao.inserirRegistros(id, delegados);
        }
        ListaPresenca criada = dao.buscarLista(id);
        return criada != null ? criada : lista;
    }

    public ListaPresenca atualizarLista(ListaPresenca lista) {
        if (lista == null || lista.getId() == null) {
            return null;
        }
        int updated = dao.atualizarLista(lista);
        if (updated == 0) {
            return null;
        }
        ListaPresenca atualizada = dao.buscarLista(lista.getId());
        return atualizada != null ? atualizada : lista;
    }

    public boolean removerLista(long id) {
        return dao.removerLista(id) > 0;
    }

    public void atualizarRegistros(long listaId, List<RegistroPresenca> registros) {
        if (registros == null || registros.isEmpty()) {
            return;
        }
        for (RegistroPresenca registro : registros) {
            int updated = dao.atualizarRegistro(listaId, registro);
            if (updated == 0) {
                dao.inserirRegistro(listaId, registro);
            }
        }
    }
}
