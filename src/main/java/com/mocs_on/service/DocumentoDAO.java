package com.mocs_on.service;

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

        String sql = """
            SELECT id, nome, autor, ativo, status, link_documento, arquivo
            FROM documentos
            WHERE ativo = true
        """;

        return jdbcTemplate.query(sql, (resultado, linha) -> {
            Documento doc = new Documento();

            doc.setId(resultado.getLong("id"));
            doc.setNome(resultado.getString("nome"));
            doc.setAutor(resultado.getString("autor"));
            doc.setAtivo(resultado.getBoolean("ativo"));
            doc.setStatus(StatusDocumento.valueOf(resultado.getString("status")));
            doc.setArquivo(resultado.getBytes("arquivo"));

            return doc;
        });
    }

    public int inserirDocumento(Documento doc) {

        String sql = """
            INSERT INTO documentos (nome, autor, ativo, status, link_documento, arquivo)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        return jdbcTemplate.update(
                sql,
                doc.getNome(),
                doc.getAutor(),
                doc.getAtivo(),
                doc.getStatus().name(),
                doc.getArquivo()
        );
    }

    public int quantidadeDocumentos() {
        String sql = "SELECT COUNT(*) FROM documentos WHERE ativo = true";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Documento recuperarPorId(Long id) {

        String sql = """
            SELECT id, nome, autor, ativo, status, link_documento, arquivo
            FROM documentos
            WHERE id = ? AND ativo = true
        """;

        List<Documento> documentos = jdbcTemplate.query(sql, (resultado, linha) -> {

            Documento doc = new Documento();
            doc.setId(resultado.getLong("id"));
            doc.setNome(resultado.getString("nome"));
            doc.setAutor(resultado.getString("autor"));
            doc.setAtivo(resultado.getBoolean("ativo"));
            doc.setStatus(StatusDocumento.valueOf(resultado.getString("status")));
            doc.setArquivo(resultado.getBytes("arquivo"));

            return doc;
        }, id);

        return documentos.stream().findFirst().orElse(null);
    }

    public int atualizarDocumento(Documento doc) {

        String sql = """
            UPDATE documentos
            SET nome = ?, autor = ?, ativo = ?, status = ?, link_documento = ?, arquivo = ?
            WHERE id = ?
        """;

        return jdbcTemplate.update(
                sql,
                doc.getNome(),
                doc.getAutor(),
                doc.getAtivo(),
                doc.getStatus().name(),
                doc.getArquivo(),
                doc.getId()
        );
    }

    public int deletarDocumento(Long id) {
        String sql = "UPDATE documentos SET ativo = false WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
