package com.mocs_on.dto;

public class InformacoesUsuarioDTO {
    private String nome;
    private boolean isSecretario;

    public InformacoesUsuarioDTO(String nome, boolean isSecretario) {
        this.nome = nome;
        this.isSecretario = isSecretario;
    }

    public String getNome() { 
        return nome; 
    }
    public boolean getIsSecretario() { 
        return isSecretario; 
    }
}

