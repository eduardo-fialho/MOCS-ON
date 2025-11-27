package com.mocs_on.domain;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private Long postId;
    private String usuario;
    private String usuarioNome;
    private String mensagem;
    private LocalDateTime createdAt;
    private String status;

    public Comment() {}

    public Comment(Long id, Long postId, String usuario, String usuarioNome, String mensagem, LocalDateTime createdAt, String status) {
        this.id = id;
        this.postId = postId;
        this.usuario = usuario;
        this.usuarioNome = usuarioNome;
        this.mensagem = mensagem;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
