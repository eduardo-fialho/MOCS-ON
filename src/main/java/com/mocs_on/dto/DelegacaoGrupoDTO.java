package com.mocs_on.dto;

import java.util.ArrayList;
import java.util.List;

public class DelegacaoGrupoDTO {
    private Long id;
    private String nome;
    private List<DelegadoResumoDTO> delegados;

    public DelegacaoGrupoDTO() {
        this.delegados = new ArrayList<>();
    }

    public DelegacaoGrupoDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
        this.delegados = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<DelegadoResumoDTO> getDelegados() {
        return delegados;
    }

    public void setDelegados(List<DelegadoResumoDTO> delegados) {
        this.delegados = delegados == null ? new ArrayList<>() : delegados;
    }

    public void addDelegado(DelegadoResumoDTO delegado) {
        if (delegado == null) {
            return;
        }
        if (this.delegados == null) {
            this.delegados = new ArrayList<>();
        }
        this.delegados.add(delegado);
    }
}
