package com.mocs_on.dto;

public class ComiteCatalogDTO {
    private Long id;
    private String nome;
    private String sigla;
    private String descricao;
    private String status;
    private Integer numeroDelegados;

    public ComiteCatalogDTO() {
    }

    public ComiteCatalogDTO(Long id, String nome, String sigla, String descricao, String status, Integer numeroDelegados) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.descricao = descricao;
        this.status = status;
        this.numeroDelegados = numeroDelegados;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNumeroDelegados() {
        return numeroDelegados;
    }

    public void setNumeroDelegados(Integer numeroDelegados) {
        this.numeroDelegados = numeroDelegados;
    }
}
