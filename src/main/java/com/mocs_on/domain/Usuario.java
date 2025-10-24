
package com.mocs_on.domain;

public class Usuario {
    String nome;
    String email;
    Integer id;
    
    Usuario(Integer id, String nome, String email){
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
    
    Usuario(){
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
