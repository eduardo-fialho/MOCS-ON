package com.mocs_on.domain;

import java.time.LocalDateTime;

public class Curtida {
    private Long id;
    private Long postId;
    private String usuario;
    private String usuarioNome;
    private LocalDateTime createdAt;

    public Curtida() {}

    public Curtida(Long id, Long postId, String usuario, String usuarioNome, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.usuario = usuario;
        this.usuarioNome = usuarioNome;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
