package com.mocs_on.domain;

public class AgendaDiaria {
    private Long id;
    private String titulo;
    private String descricao;
    private String data_evento;
    private String hora_evento;
    private Boolean visivel = true;

    public AgendaDiaria() {
        this.visivel = true;
    }
    
    public AgendaDiaria(String titulo, String descricao, String data_evento, String hora_evento) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data_evento = data_evento;
        this.hora_evento = hora_evento;
        this.visivel = true;
    }

    public AgendaDiaria(String titulo, String descricao, String data_evento, String hora_evento, Boolean visivel) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data_evento = data_evento;
        this.hora_evento = hora_evento;
        this.visivel = (visivel == null ? true : visivel);
    }

    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id;
    }

    public String getTitulo() { 
        return titulo; 
    }
    public void setTitulo(String titulo) { 
        this.titulo = titulo; 
    }

    public String getDescricao() { 
        return descricao; 
    }
    public void setDescricao(String descricao) { 
        this.descricao = descricao; 
    }

    public String getData_evento() { 
        return data_evento; 
    }
    public void setData_evento(String data_evento) { 
        this.data_evento = data_evento; 
    }

    public String getHora_evento() { 
        return hora_evento; 
    }
    public void setHora_evento(String hora_evento) { 
        this.hora_evento = hora_evento; 
    }

    public Boolean getVisivel() {
        return visivel;
    }
    public void setVisivel(Boolean visivel) {
        this.visivel = visivel;
    }
}

