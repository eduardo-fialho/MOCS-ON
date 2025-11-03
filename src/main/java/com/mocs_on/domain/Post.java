
package com.mocs_on.domain;
import java.time.LocalDateTime;

public class Post {
    private String mensagem;
    private String nomeRemetente;
    private String[] linksRelacionados;
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
        this.linksRelacionados = new String[0];
        this.status = PostStatus.EM_ANALISE;
        this.dataPublicacao = LocalDateTime.now();
    }
    
    public Post(String mensagem, String nome, String[] links, PostStatus status, LocalDateTime data){
        this.mensagem = mensagem;
        this.nomeRemetente = nome;
        this.linksRelacionados = links;
        this.status = status;
        this.dataPublicacao = data;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getNome_remetente() {
        return nomeRemetente;
    }

    public void setNome_remetente(String nomeRemetente) {
        this.nomeRemetente = nomeRemetente;
    }

    public String[] getLinks_relacionados() {
        return linksRelacionados;
    }

    public void setLinks_relacionados(String[] linksRelacionados) {
        this.linksRelacionados = linksRelacionados;
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
