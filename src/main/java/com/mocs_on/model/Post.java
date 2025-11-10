
package com.mocs_on.model;
import java.time.LocalDateTime;

public class Post {
    private String mensagem;
    private String nomeRemetente;
    private String[] linksRelacionados;
    private PostStatus status;
    private LocalDateTime dataPublicacao;
    private Reacoes reacoes;
    private int id;
    private int comiteId;
    private class Reacoes{
        public Reacoes(int like, int coracao, int riso, int surpresa, int triste, int raiva){
            this.like=like;
            this.coracao=coracao;
            this.riso=riso;
            this.surpresa=surpresa;
            this.triste=triste;
            this.raiva=raiva;

        }
        int like;
        int coracao;
        int riso;
        int surpresa;
        int triste;
        int raiva;

    }
    private enum PostStatus{
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
    public Post(String mensagem, String nome, String[] links, String status, LocalDateTime data, int like, int coracao, int riso, int surpresa, int triste, int raiva){
        this.mensagem = mensagem;
        this.nomeRemetente = nome;
        this.linksRelacionados = links;
        this.status = PostStatus.valueOf(status);
        this.dataPublicacao = data;
        setReacoes(like, coracao, riso, surpresa, triste, raiva);
    }
        public Post(String mensagem, String nome, String[] links, String status, LocalDateTime data){
        this.mensagem = mensagem;
        this.nomeRemetente = nome;
        this.linksRelacionados = links;
        this.status = PostStatus.valueOf(status);
        this.dataPublicacao = data;
        setReacoes(0, 0, 0, 0, 0, 0);
    }
    public int getId(){
        return id;
    }
    public String getMensagem() {
        return mensagem;
    }
    public void setId(int id){
        this.id=id;
    }
    public void setReacoes(int like, int coracao, int riso, int surpresa, int triste, int raiva){
        reacoes=new Reacoes(like, coracao, riso, surpresa, triste, raiva);
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

    public String getStatus() {
        return status.name();
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
