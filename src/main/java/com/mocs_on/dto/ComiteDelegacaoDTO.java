package com.mocs_on.dto;

public class ComiteDelegacaoDTO {
    private Long id;
    private String nome;
    private String sigla;
    private String delegacaoNome;

    public ComiteDelegacaoDTO() {
    }

    public ComiteDelegacaoDTO(Long id, String nome, String sigla, String delegacaoNome) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.delegacaoNome = delegacaoNome;
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

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getDelegacaoNome() {
        return delegacaoNome;
    }

    public void setDelegacaoNome(String delegacaoNome) {
        this.delegacaoNome = delegacaoNome;
    }
}
