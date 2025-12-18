package com.mocs_on.domain;

public class LinkGuia {
    
    private Long id;
    private Long id_guia;
    private String link;
    private Boolean ativo;

    public LinkGuia() {};

    public LinkGuia(Long id_guia, String link) {
        this.id_guia = id_guia;
        this.link = link;
        this.ativo = true;
    }

    public LinkGuia(String link) {
        this.link = link;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId_guia() {
        return id_guia;
    }

    public void setId_guia(Long id_guia) {
        this.id_guia = id_guia;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
