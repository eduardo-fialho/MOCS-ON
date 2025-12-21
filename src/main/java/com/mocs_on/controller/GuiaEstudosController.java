package com.mocs_on.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mocs_on.domain.GuiaEstudos;
import com.mocs_on.domain.LinkGuia;
import com.mocs_on.service.GuiaEstudosDAO;

@RestController
@RequestMapping("/guia-estudos")
public class GuiaEstudosController {

    @Autowired
    private GuiaEstudosDAO guiasService;

    @GetMapping
    public ResponseEntity<List<GuiaEstudos>> recuperarGuiasDeEstudos() {
        List<GuiaEstudos> guias = guiasService.recuperarTodos();
        return ResponseEntity.ok(guias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuiaEstudos> recuperarGuiaPorId(@PathVariable Long id) {
        GuiaEstudos guia = guiasService.recuperarPorId(id);
        if (guia != null) {
            return ResponseEntity.ok(guia);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{id}/visualizar")
    public ResponseEntity<byte[]> visualizarArquivo(@PathVariable Long id) {
        byte[] arquivo = guiasService.recuperarArquivoPorId(id);
        if (arquivo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=\"guia_" + id + ".pdf\"")
                .header("Content-Type", "application/pdf")
                .body(arquivo);
    }

    @GetMapping("/{id}/arquivo")
    public ResponseEntity<byte[]> baixarArquivo(@PathVariable Long id) {
        byte[] arquivo = guiasService.recuperarArquivoPorId(id);
        if (arquivo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"guia_" + id + ".pdf\"")
                .header("Content-Type", "application/pdf")
                .body(arquivo);
    }

    @PostMapping
    public ResponseEntity<String> criarGuiaDeEstudos(
            @RequestParam String autor,
            @RequestParam String titulo,
            @RequestParam String conteudo,
            @RequestParam String regras,
            @RequestParam(required = false) List<String> links,
            @RequestParam(required = false) MultipartFile arquivo,
            @RequestParam Long id_comite) {

        if (arquivo != null && !arquivo.isEmpty()) {
            String contentType = arquivo.getContentType();
            String originalName = arquivo.getOriginalFilename();
            boolean isPdf = "application/pdf".equalsIgnoreCase(contentType)
                    || (originalName != null && originalName.toLowerCase().endsWith(".pdf"));

            if (!isPdf) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Arquivo inválido. Apenas PDF é permitido.");
            }
        }

        GuiaEstudos guia = new GuiaEstudos();
        guia.setAutor(autor);
        guia.setTitulo(titulo);
        guia.setConteudo(conteudo);
        guia.setRegras(regras);

        List<LinkGuia> linksGuia = new ArrayList<>();
        if (links != null) {
            for (String link : links) {
                linksGuia.add(new LinkGuia(link));
            }
            guia.setLinks(linksGuia);
        }

        try {
            guia.setArquivo(arquivo != null ? arquivo.getBytes() : null);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar o arquivo");
        }

        guia.setData(LocalDateTime.now());
        guia.setAtualizadoEm(LocalDateTime.now());
        guia.setOficial(true);
        guia.setAtivo(true);
        guia.setIdComite(id_comite);

        Long idGuia = guiasService.criarGuia(guia);
        if (idGuia != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Guia criado com sucesso");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar guia");
    }

    @PostMapping("/{id}/atualizar")
    public ResponseEntity<String> atualizarGuia(
            @PathVariable Long id,
            @RequestParam String autor,
            @RequestParam String titulo,
            @RequestParam String conteudo,
            @RequestParam String regras,
            @RequestParam(required = false) List<String> links,
            @RequestParam(required = false) MultipartFile arquivo,
            @RequestParam Long id_comite) {

        if (arquivo != null && !arquivo.isEmpty()) {
            String contentType = arquivo.getContentType();
            String originalName = arquivo.getOriginalFilename();
            boolean isPdf = "application/pdf".equalsIgnoreCase(contentType)
                    || (originalName != null && originalName.toLowerCase().endsWith(".pdf"));

            if (!isPdf) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Arquivo inválido. Apenas PDF é permitido.");
            }
        }

        GuiaEstudos guia = new GuiaEstudos();
        guia.setAutor(autor);
        guia.setTitulo(titulo);
        guia.setConteudo(conteudo);
        guia.setRegras(regras);

        List<LinkGuia> linksGuia = new ArrayList<>();
        if (links != null) {
            for (String link : links) {
                linksGuia.add(new LinkGuia(id, link));
            }
            guia.setLinks(linksGuia);
        }

        try {
            if (arquivo != null) {
                guia.setArquivo(arquivo.getBytes());
            }
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar arquivo");
        }

        guia.setAtualizadoEm(LocalDateTime.now());
        guia.setOficial(true);
        guia.setAtivo(true);
        guia.setIdComite(id_comite);

        int linhas = guiasService.atualizarPorId(id, guia);
        if (linhas == 1) {
            return ResponseEntity.ok("Guia atualizado com sucesso");
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao atualizar guia");
    }

    @GetMapping("/{id}/deletar")
    public ResponseEntity<String> deletarGuia(@PathVariable Long id) {
        int linhas = guiasService.desativarPorId(id);
        if (linhas == 1) {
            return ResponseEntity.ok("Guia deletado com sucesso");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar guia");
    }
}
