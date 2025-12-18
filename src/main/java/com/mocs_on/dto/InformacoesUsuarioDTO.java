package com.mocs_on.dto;

public class InformacoesUsuarioDTO {
    private String nome;
    private boolean isSecretario;
    private String email;

    public InformacoesUsuarioDTO(String nome, boolean isSecretario, String email) {
        this.nome = nome;
        this.isSecretario = isSecretario;
        this.email = email;
    }

    public String getNome() { 
        return nome; 
    }
    public boolean getIsSecretario() { 
        return isSecretario; 
    }
    public String getEmail() { 
        return email; 
    }
}

