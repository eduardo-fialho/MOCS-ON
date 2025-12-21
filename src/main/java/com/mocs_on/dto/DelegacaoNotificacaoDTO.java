package com.mocs_on.dto;

public class DelegacaoNotificacaoDTO {

    private long usuarioId;
    private long comiteId;
    private String mensagem;

    public DelegacaoNotificacaoDTO(long usuarioId, long comiteId, String mensagem) {
        this.usuarioId = usuarioId;
        this.comiteId = comiteId;
        this.mensagem = mensagem;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public long getComiteId() {
        return comiteId;
    }

    public String getMensagem() {
        return mensagem;
    }
}
