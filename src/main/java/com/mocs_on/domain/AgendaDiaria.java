package com.mocs_on.domain;

public class AgendaDiaria {
    private Long id;
    private String titulo;
    private String descricao;
    private String data;
    private String hora;

    public AgendaDiaria() {}

    public AgendaDiaria(String titulo, String descricao, String data, String hora) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.hora = hora;
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


    public String getData() { 
        return data;
    }
    public void setData(String data) { 
        this.data = data; 
    }

    
    public String getHora() { 
        return hora; 
    }
    public void setHora(String hora) { 
        this.hora = hora; 
    }

}

