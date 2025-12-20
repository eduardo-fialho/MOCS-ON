package com.mocs_on.dto;

import java.util.List;

import com.mocs_on.domain.ListaPresenca;
import com.mocs_on.domain.RegistroPresenca;

public class PresencaDetalheDTO {
    private ListaPresenca lista;
    private List<RegistroPresenca> registros;

    public PresencaDetalheDTO() {
    }

    public PresencaDetalheDTO(ListaPresenca lista, List<RegistroPresenca> registros) {
        this.lista = lista;
        this.registros = registros;
    }

    public ListaPresenca getLista() {
        return lista;
    }

    public void setLista(ListaPresenca lista) {
        this.lista = lista;
    }

    public List<RegistroPresenca> getRegistros() {
        return registros;
    }

    public void setRegistros(List<RegistroPresenca> registros) {
        this.registros = registros;
    }
}
