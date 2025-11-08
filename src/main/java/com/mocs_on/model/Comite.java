
package com.mocs_on.model;
import java.util.HashMap;
public class Comite {
    
    private String sigla;
    private String nome;
    private StatusComite status;
    private int numeroDelegados;
    private int id;
    private Post[] posts;

    private enum StatusComite {
        EM_ANDAMENTO,
        NAO_INICIADO,
        ENCERRADO
    }
    public String getNome(){
        return nome;
    }
    public String getSigla(){
        return sigla;
    }
    public String getStatus(){
        return status.name();
    }
    public int getNumDelegados(){
        return numeroDelegados;
    }
    public int getId(){
        return id;
    }
    public void setNome(String nome){
        this.nome=nome;
    }
    public void setSigla(String sigla){
        this.sigla=sigla;
    }
    public void setStatus(String status){
        this.status=StatusComite.valueOf(status);
    }
    public void setNumDelegados(int numeroDelegados){
        this.numeroDelegados=numeroDelegados;
    }
    public void setId(int id){
        this.id=id;
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
    
    public Comite(String sigla, String nome, String status){
        this.sigla = sigla;
        this.nome = nome;
        this.status = StatusComite.valueOf(status);
        this.numeroDelegados = 0;
    }
    
    public Comite(String sigla, String nome, String status, int numeroDelegados){
        this.sigla = sigla;
        this.nome = nome;
        this.status = StatusComite.valueOf(status);
        this.numeroDelegados = numeroDelegados;
    }
}
