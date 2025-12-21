package com.mocs_on.domain;

public class ListaPresenca {
    private Long id;
    private Long comiteId;
    private String comiteNome;
    private String comiteSigla;
    private String titulo;
    private String dataSessao;
    private String horaInicio;
    private String horaFim;
    private String observacao;
    private String criadoPor;
    private String criadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComiteId() {
        return comiteId;
    }

    public void setComiteId(Long comiteId) {
        this.comiteId = comiteId;
    }

    public String getComiteNome() {
        return comiteNome;
    }

    public void setComiteNome(String comiteNome) {
        this.comiteNome = comiteNome;
    }

    public String getComiteSigla() {
        return comiteSigla;
    }

    public void setComiteSigla(String comiteSigla) {
        this.comiteSigla = comiteSigla;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDataSessao() {
        return dataSessao;
    }

    public void setDataSessao(String dataSessao) {
        this.dataSessao = dataSessao;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public String getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }
}
