package com.mocs_on.domain;

import java.time.LocalDateTime;

public class Documento {

    private Long id;
    private String nome;
    private String autor;
    private Boolean ativo;
    private StatusDocumento status;
    private byte[] arquivo;
    private LocalDateTime data;
    private String avaliacao;

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
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

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public StatusDocumento getStatus() {
        return status;
    }

    public void setStatus(StatusDocumento status) {
        this.status = status;
    }

    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }
}
