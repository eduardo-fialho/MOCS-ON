package com.mocs_on.service;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mocs_on.domain.GuiaEstudos;
import com.mocs_on.domain.LinkGuia;

@Repository
public class GuiaEstudosDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<GuiaEstudos> recuperarTodos() {
        String sql = """
            SELECT id, autor, titulo, conteudo, regras, arquivo,
                `data`, atualizado_em, oficial, ativo, id_comite
            FROM guia_estudos
            WHERE ativo = true
            ORDER BY data DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            GuiaEstudos guia = mapearGuia(rs);
            guia.setLinks(recuperarLinksPorGuiaId(guia.getId()));
            return guia;
        });
    }

    public GuiaEstudos recuperarPorId(Long id) {
        String sql = """
            SELECT id, autor, titulo, conteudo, regras, arquivo,
                `data`, atualizado_em, oficial, ativo, id_comite
            FROM guia_estudos
            WHERE id = ? AND ativo = true
        """;

        List<GuiaEstudos> guias = jdbcTemplate.query(sql, (rs, rowNum) -> {
            GuiaEstudos guia = mapearGuia(rs);
            guia.setLinks(recuperarLinksPorGuiaId(guia.getId()));
            return guia;
        }, id);

        return guias.stream().findFirst().orElse(null);
    }

    public byte[] recuperarArquivoPorId(Long id) {
        String sql = """
            SELECT arquivo
            FROM guia_estudos
            WHERE id = ? AND ativo = true
        """;

        return jdbcTemplate.query(
            sql,
            rs -> rs.next() ? rs.getBytes("arquivo") : null,
            id
        );
    }

    @Transactional
    public Long criarGuia(GuiaEstudos guia) {
        String sqlGuia = """
            INSERT INTO guia_estudos
            (autor, titulo, conteudo, regras, arquivo, `data`,
             atualizado_em, oficial, ativo, id_comite)
            VALUES (?, ?, ?, ?, ?, ?,
                    ?, ?, true, ?)
        """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sqlGuia, new String[]{"id"});
            ps.setString(1, guia.getAutor());
            ps.setString(2, guia.getTitulo());
            ps.setString(3, guia.getConteudo());
            ps.setString(4, guia.getRegras());
            ps.setBytes(5, guia.getArquivo());
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(8, guia.getOficial() != null && guia.getOficial());
            ps.setObject(9, guia.getIdComite(), Types.BIGINT);
            return ps;
        }, keyHolder);

        Long idGuia = keyHolder.getKey().longValue();

        if (guia.getLinks() != null && !guia.getLinks().isEmpty()) {
            inserirLinks(idGuia, guia.getLinks());
        }

        return idGuia;
    }

    @Transactional
    public int atualizarPorId(Long id, GuiaEstudos guia) {
        String sqlGuia = """
            UPDATE guia_estudos
            SET autor = ?,
                titulo = ?,
                conteudo = ?,
                regras = ?,
                arquivo = ?,
                oficial = ?,
                id_comite = ?,
                atualizado_em = ?
            WHERE id = ?
        """;

        int rows = jdbcTemplate.update(
            sqlGuia,
            guia.getAutor(),
            guia.getTitulo(),
            guia.getConteudo(),
            guia.getRegras(),
            guia.getArquivo(),
            guia.getOficial(),
            guia.getIdComite(),
            LocalDateTime.now(),
            id
        );

        jdbcTemplate.update("UPDATE link_guia SET ativo = false WHERE id_guia = ?", id);

        if (guia.getLinks() != null) {
            inserirLinks(id, guia.getLinks());
        }

        return rows;
    }

    @Transactional
    public int desativarPorId(Long id) {
        String sql = """
            UPDATE guia_estudos
            SET ativo = false,
                atualizado_em = ?
            WHERE id = ?
        """;

        return jdbcTemplate.update(sql, LocalDateTime.now(), id);
    }

    private GuiaEstudos mapearGuia(java.sql.ResultSet rs) throws java.sql.SQLException {
        GuiaEstudos guia = new GuiaEstudos();
        guia.setId(rs.getLong("id"));
        guia.setAutor(rs.getString("autor"));
        guia.setTitulo(rs.getString("titulo"));
        guia.setConteudo(rs.getString("conteudo"));
        guia.setRegras(rs.getString("regras"));
        guia.setArquivo(rs.getBytes("arquivo"));
        guia.setOficial(rs.getBoolean("oficial"));
        guia.setAtivo(rs.getBoolean("ativo"));
        guia.setIdComite(rs.getObject("id_comite") != null ? rs.getLong("id_comite") : null);

        Timestamp dataTs = rs.getTimestamp("data");
        guia.setData(dataTs != null ? dataTs.toLocalDateTime() : null);

        Timestamp atualizadoTs = rs.getTimestamp("atualizado_em");
        guia.setAtualizadoEm(atualizadoTs != null ? atualizadoTs.toLocalDateTime() : null);

        return guia;
    }

    private List<LinkGuia> recuperarLinksPorGuiaId(Long idGuia) {
        String sql = """
            SELECT id, id_guia, link, ativo
            FROM link_guia
            WHERE id_guia = ? AND ativo = true
            ORDER BY id
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LinkGuia link = new LinkGuia();
            link.setId(rs.getLong("id"));
            link.setId_guia(rs.getLong("id_guia"));
            link.setLink(rs.getString("link"));
            link.setAtivo(rs.getBoolean("ativo"));
            return link;
        }, idGuia);
    }

    private void inserirLinks(Long idGuia, List<LinkGuia> links) {
        String sql = """
            INSERT INTO link_guia (id_guia, link, ativo)
            VALUES (?, ?, ?)
        """;

        for (LinkGuia link : links) {
            jdbcTemplate.update(
                sql,
                idGuia,
                link.getLink(),
                link.getAtivo() != null ? link.getAtivo() : true
            );
        }
    }
}
