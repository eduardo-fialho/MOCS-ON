package com.mocs_on.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.mocs_on.domain.Documento;
import com.mocs_on.domain.StatusDocumento;

@Repository
public class DocumentoDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Documento> recuperarTodos() {
        String sql = "SELECT id, nome, autor, ativo, status, arquivo, data, avaliacao, comite_sigla FROM documentos WHERE ativo = true";

        return jdbcTemplate.query(sql, (resultado, linha) -> {
            Documento doc = new Documento();
            doc.setId(resultado.getLong("id"));
            doc.setNome(resultado.getString("nome"));
            doc.setAutor(resultado.getString("autor"));
            doc.setAtivo(resultado.getBoolean("ativo"));
            String statusStr = resultado.getString("status");
            try {
                doc.setStatus(statusStr != null ? StatusDocumento.valueOf(statusStr) : StatusDocumento.RECEBIDO);
            } catch (IllegalArgumentException e) {
                doc.setStatus(StatusDocumento.RECEBIDO);
            }

            Timestamp ts = resultado.getTimestamp("data");
            doc.setData(ts != null ? ts.toLocalDateTime() : null);
            doc.setArquivo(resultado.getBytes("arquivo"));
            doc.setAvaliacao(resultado.getString("avaliacao"));
            doc.setComiteSigla(resultado.getString("comite_sigla"));

            return doc;
        });
    }

    public Documento recuperarPorId(Long id) {
        String sql = "SELECT id, nome, autor, ativo, status, arquivo, data, avaliacao, comite_sigla FROM documentos WHERE id = ? AND ativo = true";

        List<Documento> documentos = jdbcTemplate.query(sql, (resultado, linha) -> {
            Documento doc = new Documento();
            doc.setId(resultado.getLong("id"));
            doc.setNome(resultado.getString("nome"));
            doc.setAutor(resultado.getString("autor"));
            doc.setAtivo(resultado.getBoolean("ativo"));

            String statusStr = resultado.getString("status");
            try {
                doc.setStatus(statusStr != null ? StatusDocumento.valueOf(statusStr) : StatusDocumento.RECEBIDO);
            } catch (IllegalArgumentException e) {
                doc.setStatus(StatusDocumento.RECEBIDO);
            }

            Timestamp ts = resultado.getTimestamp("data");
            doc.setData(ts != null ? ts.toLocalDateTime() : null);

            doc.setArquivo(resultado.getBytes("arquivo"));
            doc.setAvaliacao(resultado.getString("avaliacao"));
            doc.setComiteSigla(resultado.getString("comite_sigla"));

            return doc;
        }, id);

        return documentos.stream().findFirst().orElse(null);
    }

    public int inserirDocumento(Documento doc) {
        String sql = """
            INSERT INTO documentos (nome, autor, ativo, status, arquivo, avaliacao)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        return jdbcTemplate.update(
            sql,
            doc.getNome(),
            doc.getAutor(),
            doc.getAtivo(),
            doc.getStatus().name(),
            doc.getArquivo(),
            doc.getAvaliacao()
        );
    }

    public int atualizarDocumento(Documento doc) {
        String sql = "UPDATE documentos SET nome = ?, autor = ?, ativo = ?, status = ?, arquivo = ?, avaliacao = ?, comite_sigla = ? WHERE id = ?";

        return jdbcTemplate.update(
            sql,
            doc.getNome(),
            doc.getAutor(),
            doc.getAtivo(),
            doc.getStatus().name(),
            doc.getArquivo(),
            doc.getAvaliacao(),
            doc.getComiteSigla(),
            doc.getId()
        );
    }

    public Long quantidadeDocumentos() {
        String sql = "SELECT COUNT(*) FROM documentos WHERE ativo = true";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public static StatusDocumento fromString(String status) {
        for (StatusDocumento s : StatusDocumento.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Status inválido: " + status);
    }
}