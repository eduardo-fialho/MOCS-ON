package com.mocs_on.domain;

import java.time.LocalDateTime;

public class OuvidoriaRelato {

    private Long id;
    private LocalDateTime createdAt;
    private String status;

    private String identificacao;
    private String nomeRelator;
    private String comiteRelator;

    private String categoriaRelato;

    private String comiteConducao;
    private String comiteRespeito;
    private String comiteImparcialidade;
    private String comiteApoio;
    private String comiteMensagem;

    private String secretariadoPositivos;
    private String secretariadoNegativos;
    private String secretariadoFalta;
    private String secretariadoSugestoes;

    private String outrosMensagem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdentificacao() {
        return identificacao;
    }

    public void setIdentificacao(String identificacao) {
        this.identificacao = identificacao;
    }

    public String getNomeRelator() {
        return nomeRelator;
    }

    public void setNomeRelator(String nomeRelator) {
        this.nomeRelator = nomeRelator;
    }

    public String getComiteRelator() {
        return comiteRelator;
    }

    public void setComiteRelator(String comiteRelator) {
        this.comiteRelator = comiteRelator;
    }

    public String getCategoriaRelato() {
        return categoriaRelato;
    }

    public void setCategoriaRelato(String categoriaRelato) {
        this.categoriaRelato = categoriaRelato;
    }

    public String getComiteConducao() {
        return comiteConducao;
    }

    public void setComiteConducao(String comiteConducao) {
        this.comiteConducao = comiteConducao;
    }

    public String getComiteRespeito() {
        return comiteRespeito;
    }

    public void setComiteRespeito(String comiteRespeito) {
        this.comiteRespeito = comiteRespeito;
    }

    public String getComiteImparcialidade() {
        return comiteImparcialidade;
    }

    public void setComiteImparcialidade(String comiteImparcialidade) {
        this.comiteImparcialidade = comiteImparcialidade;
    }

    public String getComiteApoio() {
        return comiteApoio;
    }

    public void setComiteApoio(String comiteApoio) {
        this.comiteApoio = comiteApoio;
    }

    public String getComiteMensagem() {
        return comiteMensagem;
    }

    public void setComiteMensagem(String comiteMensagem) {
        this.comiteMensagem = comiteMensagem;
    }

    public String getSecretariadoPositivos() {
        return secretariadoPositivos;
    }

    public void setSecretariadoPositivos(String secretariadoPositivos) {
        this.secretariadoPositivos = secretariadoPositivos;
    }

    public String getSecretariadoNegativos() {
        return secretariadoNegativos;
    }

    public void setSecretariadoNegativos(String secretariadoNegativos) {
        this.secretariadoNegativos = secretariadoNegativos;
    }

    public String getSecretariadoFalta() {
        return secretariadoFalta;
    }

    public void setSecretariadoFalta(String secretariadoFalta) {
        this.secretariadoFalta = secretariadoFalta;
    }

    public String getSecretariadoSugestoes() {
        return secretariadoSugestoes;
    }

    public void setSecretariadoSugestoes(String secretariadoSugestoes) {
        this.secretariadoSugestoes = secretariadoSugestoes;
    }

    public String getOutrosMensagem() {
        return outrosMensagem;
    }

    public void setOutrosMensagem(String outrosMensagem) {
        this.outrosMensagem = outrosMensagem;
    }
}
