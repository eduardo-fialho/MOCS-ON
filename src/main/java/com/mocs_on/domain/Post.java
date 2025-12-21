package com.mocs_on.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Post {
    private Long id;
    private String mensagem;
    private String autor;
    private TipoPost status;
    private LocalDateTime dataPublicacao;
<<<<<<< HEAD
    private String comiteSigla;
    private String aprovador; // email do diretor que aprovou, se aplicável
=======
    private Long comiteId;
>>>>>>> origin/main

    private String myReaction;

    public static final String DISPLAY_ANON = "Spotted by: CSNU";
    private Map<String, Integer> reactions = new HashMap<>();

    public enum TipoPost {
        PUBLICO,
        ANONIMO,
        CONSULTA_INFORMAL,
        EXCLUIDO,
        PENDENTE,
    }

    public Post() {
        this.mensagem = "";
        this.autor = "";
        this.status = TipoPost.PUBLICO;
        this.dataPublicacao = LocalDateTime.now();
    }

    public Post(String mensagem, String nome, TipoPost status, LocalDateTime data) {
        this.mensagem = mensagem;
        this.autor = nome;
        this.status = status;
        this.dataPublicacao = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getAutor() {
        if (status == TipoPost.ANONIMO) {
            return DISPLAY_ANON;
        }
        return autor;
    }

    /** Retorna o autor conforme armazenado, sem anonimizacao. */
    public String getAutorRaw() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public TipoPost getStatus() {
        return status;
    }

    public void setStatus(TipoPost status) {
        this.status = status;
    }

    public LocalDateTime getData() {
        return dataPublicacao;
    }

    public void setData(LocalDateTime dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getComiteSigla() {
        return comiteSigla;
    }

    public void setComiteSigla(String comiteSigla) {
        this.comiteSigla = comiteSigla;
    }

    public String getAprovador() {
        return aprovador;
    }

    public void setAprovador(String aprovador) {
        this.aprovador = aprovador;
    }

    public Map<String, Integer> getReactions() {
        return Collections.unmodifiableMap(reactions);
    }

    public void setReactions(Map<String, Integer> reactions) {
        this.reactions = new HashMap<>(reactions != null ? reactions : Collections.emptyMap());
    }

    public void addReaction(String emoji) {
        reactions.merge(emoji, 1, Integer::sum);
    }

    public void removeReaction(String emoji) {
        reactions.computeIfPresent(emoji, (k, v) -> (v <= 1) ? null : v - 1);
    }

    public String getMyReaction() {
        return myReaction;
    }

    public void setMyReaction(String myReaction) {
        this.myReaction = myReaction;
    }
    public Long getComiteId() {
        return comiteId;
    }
    public void setComiteId(Long comiteId) {
        this.comiteId = comiteId;
    }
}

