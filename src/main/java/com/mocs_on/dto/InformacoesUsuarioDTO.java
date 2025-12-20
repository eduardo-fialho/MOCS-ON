package com.mocs_on.dto;

public class InformacoesUsuarioDTO {
    private String nome;
    private String email;
    private boolean isSecretario;

    public InformacoesUsuarioDTO(String nome, String email, boolean isSecretario) {
        this.nome = nome;
        this.email = email;
        this.isSecretario = isSecretario;
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
