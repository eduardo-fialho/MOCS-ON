
package com.mocs_on.domain;

import java.time.LocalDateTime;

public class Reaction {
    private Long id;
    private Long postId;
    private String usuario;
    private String emoji;
    private LocalDateTime createdAt;
    
    public Reaction(Long id, Long postId, String usuario, String emoji, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.usuario = usuario;
        this.emoji = emoji;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Reaction{" +
                "id=" + id +
                ", postId=" + postId +
                ", usuario='" + usuario + '\'' +
                ", emoji='" + emoji + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

