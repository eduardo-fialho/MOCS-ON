package com.mocs_on.domain;

import java.time.LocalDateTime;
import java.util.List;

public class GuiaEstudos {
    
    private Long id;
    private String autor;
    private String titulo;
    private String conteudo;
    private String regras;
    private List<LinkGuia> links;
    private byte[] arquivo;
    private LocalDateTime data;
    private LocalDateTime atualizadoEm;
    private Boolean oficial;
    private Boolean ativo;
    private Long id_comite;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public List<LinkGuia> getLinks() {
        return links;
    }

    public void setLinks(List<LinkGuia> links) {
        this.links = links;
    }

    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Boolean getOficial() {
        return oficial;
    }

    public void setOficial(Boolean oficial) {
        this.oficial = oficial;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Long getId_comite() {
        return id_comite;
    }

    public void setId_comite(Long id_comite) {
        this.id_comite = id_comite;
    }

    public String getRegras() {
        return regras;
    }

    public void setRegras(String regras) {
        this.regras = regras;
    }

}
