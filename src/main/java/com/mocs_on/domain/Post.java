
package com.mocs_on.domain;
import java.time.LocalDateTime;

public class Post {
    private String mensagem;
    private String nomeRemetente;
    private PostStatus status;
    private LocalDateTime dataPublicacao;
    
    public enum PostStatus{
        PUBLICO,
        PRIVADO,
        EM_ANALISE,
    }
    
    public Post() {
        this.mensagem = "";
        this.nomeRemetente = "";
        this.status = PostStatus.EM_ANALISE;
        this.dataPublicacao = LocalDateTime.now();
    }
    
    public Post(String mensagem, String nome, PostStatus status, LocalDateTime data){
        this.mensagem = mensagem;
        this.nomeRemetente = nome;
        this.status = status;
        this.dataPublicacao = data;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getNomeRemetente() {
        return nomeRemetente;
    }

    public void setNomeRemetente(String nomeRemetente) {
        this.nomeRemetente = nomeRemetente;
    }

    public PostStatus getStatus() {
        return status;
    }

    public void setStatus(PostStatus status) {
        this.status = status;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDateTime dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }
}
