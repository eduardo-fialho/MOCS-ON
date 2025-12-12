package com.mocs_on.dto;

public class UserSearchResultDTO {

    private Long id;
    private String nome;
    private String email;
    private String tipo;
    private String lastPostSnippet;
    private String lastPostDate;

    public UserSearchResultDTO() {
    }

    public UserSearchResultDTO(Long id,
                               String nome,
                               String email,
                               String tipo,
                               String lastPostSnippet,
                               String lastPostDate) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.tipo = tipo;
        this.lastPostSnippet = lastPostSnippet;
        this.lastPostDate = lastPostDate;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLastPostSnippet() {
        return lastPostSnippet;
    }

    public String getLastPostDate() {
        return lastPostDate;
    }
}
