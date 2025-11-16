package com.mocs_on.domain;

import java.io.Serializable;

public class Comite implements Serializable {
    
    private static final long serialVersionUID = 1L; 

    private String sigla;
    private String nome;
    private StatusComite status;
    private int numeroDelegados;
    
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
    }
    
    public Comite(String sigla, String nome){
        this.sigla = sigla;
        this.nome = nome;
        this.status = StatusComite.NAO_INICIADO;
        this.numeroDelegados = 0;
    }
    
    public Comite(String sigla, String nome, StatusComite status){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = 0;
    }
    
    public Comite(String sigla, String nome, StatusComite status, int numeroDelegados){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = numeroDelegados;
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
}