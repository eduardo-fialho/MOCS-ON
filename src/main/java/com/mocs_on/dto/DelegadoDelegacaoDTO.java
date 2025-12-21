package com.mocs_on.dto;

public class DelegadoDelegacaoDTO {

    private long usuarioId;
    private String nome;
    private String email;
    private Long delegacaoId;
    private String delegacaoNome;

    public DelegadoDelegacaoDTO(long usuarioId, String nome, String email, Long delegacaoId, String delegacaoNome) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.email = email;
        this.delegacaoId = delegacaoId;
        this.delegacaoNome = delegacaoNome;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
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

    public Long getDelegacaoId() {
        return delegacaoId;
    }

    public void setDelegacaoId(Long delegacaoId) {
        this.delegacaoId = delegacaoId;
    }

    public String getDelegacaoNome() {
        return delegacaoNome;
    }

    public void setDelegacaoNome(String delegacaoNome) {
        this.delegacaoNome = delegacaoNome;
    }
}
