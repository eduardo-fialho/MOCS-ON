
package com.mocs_on.domain;

public class Comite {
    
    String sigla;
    String nome;
    StatusComite status;
    int numeroDelegados;
    
    private enum StatusComite {
        EM_ANDAMENTO,
        NAO_INICIADO,
        ENCERRADO
    }
    
    Comite(){
        this.sigla = "";
        this.nome = "";
        this.status = StatusComite.NAO_INICIADO;
        this.numeroDelegados = 0;
    }
    
    Comite(String sigla, String nome){
        this.sigla = sigla;
        this.nome = nome;
        this.status = StatusComite.NAO_INICIADO;
        this.numeroDelegados = 0;
    }
    
    Comite(String sigla, String nome, StatusComite status){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = 0;
    }
    
    Comite(String sigla, String nome, StatusComite status, int numeroDelegados){
        this.sigla = sigla;
        this.nome = nome;
        this.status = status;
        this.numeroDelegados = numeroDelegados;
    }
    
}
