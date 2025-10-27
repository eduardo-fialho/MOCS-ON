
package com.mocs_on.domain;

public class Usuario {
    String nome;
    String email;
    Integer id;
    String senha;
    
    public Usuario(Integer id, String nome, String email, String senha){
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }
    
    public Usuario(){
        this.senha = "";
        this.nome = "";
        this.email = "";
    }
    
    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Integer getId() {
        return id;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
