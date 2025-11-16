
package com.mocs_on.model;

import java.time.LocalDateTime;
import java.util.HashMap;
public class Post {
    private String mensagem;
    private String nomeRemetente;
    private String[] linksRelacionados;
    private PostStatus postStatus;
    private LocalDateTime dataPublicacao;
    private int id;
    private HashMap<String, int> reacoes;

    private enum PostStatus {
        PUBLICO,
        PRIVADO,
        EM_ANALISE,
    }

    public Post() {
        this.mensagem = "";
        this.nomeRemetente = "";
        this.linksRelacionados = new String[0];
        this.postStatus = PostStatus.EM_ANALISE;
        this.dataPublicacao = LocalDateTime.now();
        reacoes=new HashMap<>();
    }

    public Post(String mensagem, String nome, String[] links, String status, LocalDateTime data) {
        this.mensagem = mensagem;
        this.nomeRemetente = nome;
        this.linksRelacionados = links;
        this.postStatus = PostStatus.valueOf(status);
        this.dataPublicacao = data;
        reacoes=new HashMap<>();
    }

    public Post(String mensagem, String nome, String[] links, String status, LocalDateTime data, int curtida,
            int coracao, int riso, int surpresa, int triste, int raiva, int id) {
        this.mensagem = mensagem;
        this.nomeRemetente = nome;
        this.linksRelacionados = links;
        this.postStatus = PostStatus.valueOf(status);
        this.dataPublicacao = data;
        setReacoes(curtida, coracao, riso, surpresa, triste, raiva);
        reacoes=new HashMap<>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }
    public void setReacoes(int curtida, int coracao, int riso, int surpresa, int triste, int raiva){
        reacoes.put("curtida", curtida);
        reacoes.put("coracao", coracao);
        reacoes.put("riso", riso);
        reacoes.put("surpresa", surpresa);
        reacoes.put("triste", triste);
        reacoes.put("raiva", raiva);
    }
    public HashMap<String, int> getReacoes(){
        return reacoes;
    }
    public void setNome_remetente(String nomeRemetente) {
        this.nomeRemetente = nomeRemetente;
    }

    public String getNome_remetente() {
        return nomeRemetente;
    }

    public void setLinks_relacionados(String[] linksRelacionados) {
        this.linksRelacionados = linksRelacionados;
    }

    public String[] getLinks_relacionados() {
        return linksRelacionados;
    }

    public void setStatus(String status) {
        this.postStatus = PostStatus.valueOf(status);
    }

    public String getStatus() {
        return postStatus.name();
    }

    public void setDataPublicacao(LocalDateTime dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

}
