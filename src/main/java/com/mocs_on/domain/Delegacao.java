package com.mocs_on.domain;

import java.io.Serializable;

public class Delegacao implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long comiteId;
    private String nome;

    public Delegacao() {
        this.nome = "";
    }

    public Delegacao(long id, long comiteId, String nome) {
        this.id = id;
        this.comiteId = comiteId;
        this.nome = nome;
    }

    public long getId() {
        return id;
    }

    public long getComiteId() {
        return comiteId;
    }

    public String getNome() {
        return nome;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setComiteId(long comiteId) {
        this.comiteId = comiteId;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
