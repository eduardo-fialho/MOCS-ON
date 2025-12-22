package com.mocs_on.domain;

import java.time.LocalDateTime;

public class RelatoOuvidoria {

    private Long id;
    private LocalDateTime criadoEm;
    private LocalDateTime resolvidoEm;
    private Boolean ativo;
    private StatusRelatoOuvidoria status;
    private String autor;
    private String assunto;
    private String relato;
    private String ouvidor;
    private String resposta;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    public LocalDateTime getResolvidoEm() {
        return resolvidoEm;
    }
    public void setResolvidoEm(LocalDateTime resolvidoEm) {
        this.resolvidoEm = resolvidoEm;
    }
    public Boolean getAtivo() {
        return ativo;
    }
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    public StatusRelatoOuvidoria getStatus() {
        return status;
    }
    public void setStatus(StatusRelatoOuvidoria status) {
        this.status = status;
    }
    public String getAutor() {
        return autor;
    }
    public void setAutor(String autor) {
        this.autor = autor;
    }
    public String getAssunto() {
        return assunto;
    }
    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }
    public String getRelato() {
        return relato;
    }
    public void setRelato(String relato) {
        this.relato = relato;
    }
    public String getOuvidor() {
        return ouvidor;
    }
    public void setOuvidor(String ouvidor) {
        this.ouvidor = ouvidor;
    }
    public String getResposta() {
        return resposta;
    }
    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

}
