package com.mocs_on.dto;

public class InformacoesUsuarioDTO {
    private String nome;
    private String email;
    private boolean isSecretario;

    public InformacoesUsuarioDTO(String nome, boolean isSecretario, String email) {
        this.nome = nome;
        this.email = email;
        this.isSecretario = isSecretario;
        this.email = email;
    }

    public String getNome() { 
        return nome; 
    }
    public String getEmail() {
        return email;
    }
    public boolean getIsSecretario() { 
        return isSecretario; 
    }
    
}
