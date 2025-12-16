package com.mocs_on.domain;

import java.time.LocalDateTime;

public class MateriaLog {

    private Long id;
    private Long materiaId;
    private AcaoMateriaLog acao;
    private String usuario;
    private String descricao;
    private LocalDateTime data;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMateriaId() {
        return materiaId;
    }

    public void setMateriaId(Long materiaId) {
        this.materiaId = materiaId;
    }

    public AcaoMateriaLog getAcao() {
        return acao;
    }

    public void setAcao(AcaoMateriaLog acao) {
        this.acao = acao;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
