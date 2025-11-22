package com.mocs_on.domain;

import java.time.LocalDateTime;

public class Aviso {

    private String autor;
    private String titulo;
    private String mensagem;
    private LocalDateTime data;

    public Aviso(String autor, String titulo, String mensagem, LocalDateTime data) {
        this.autor = autor;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.data = data;
    }

    public Aviso() {};

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

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }
}
