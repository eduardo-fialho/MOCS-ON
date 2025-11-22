package com.mocs_on.domain;

import java.io.Serializable;

public class Comite implements Serializable {
    
    private static final long serialVersionUID = 1L; 

    private long id;
    private String sigla;
    private String nome;
    private StatusComite status;
    private int numeroDelegados;
    private String descricao;
    
    public enum StatusComite {
        EM_ANDAMENTO,
        NAO_INICIADO,
        ENCERRADO
    }
    
    public Comite(){
        this.sigla = "";
        this.nome = "";
        this.status = StatusComite.NAO_INICIADO;
        this.numeroDelegados = 0;
        this.descricao = "";
    }
    
    public Comite(String sigla, String nome){
        this.sigla = sigla;
        this.nome = nome;
        this.status = StatusComite.NAO_INICIADO;
        this.numeroDelegados = 0;
        this.descricao = "";
    }
    
    public Comite(String sigla, String nome, StatusComite status){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = 0;
        this.descricao = "";
    }
    
    public Comite(String sigla, String nome, StatusComite status, int numeroDelegados){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = numeroDelegados;
        this.descricao = "";
    }
    
    public Comite(String sigla, String nome, StatusComite status, int numeroDelegados, String descricao){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = numeroDelegados;
        this.descricao = descricao;
    }


    public String getSigla() {
        return sigla;
    }

    public String getNome() {
        return nome;
    }

    public StatusComite getStatus() {
        return status;
    }

    public int getNumeroDelegados() {
        return numeroDelegados;
    }
    
    public String getDescricao() {
        return descricao;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setStatus(StatusComite status) {
        this.status = status;
    }

    public void setNumeroDelegados(int numeroDelegados) {
        this.numeroDelegados = numeroDelegados;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}