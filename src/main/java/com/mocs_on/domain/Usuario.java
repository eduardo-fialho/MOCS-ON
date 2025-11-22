
package com.mocs_on.domain;

import java.util.List;

import com.mocs_on.security.CargoEnum;

public class Usuario {
    
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private List<Comite> comites;
    private CargoEnum tipo;
    
    public Usuario(Long id, String nome, String email, String senha, List<Comite> comites){
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.comites = comites;
    }
    
    public Usuario(){
        this.senha = "";
        this.nome = "";
        this.email = "";
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<Comite> getComites() {
        return comites;
    }

    public CargoEnum getTipo() {
        return tipo;
    }

    public void setComites(List<Comite> comites) {
        this.comites = comites;
    }

    public void setTipo(String tipo) {
        this.tipo = CargoEnum.valueOf(tipo.toUpperCase());
    }
    
}
