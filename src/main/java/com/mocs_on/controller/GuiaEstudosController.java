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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mocs_on.domain.GuiaEstudos;
import com.mocs_on.domain.LinkGuia;
import com.mocs_on.service.GuiaEstudosDAO;

@RestController
public class GuiaEstudosController {
    
    @Autowired
    private GuiaEstudosDAO guiasService;

    @GetMapping
    public ResponseEntity<List<GuiaEstudos>> recuperarGuiasDeEstudos() {
        List<GuiaEstudos> guias = guiasService.recuperarTodos();
        return ResponseEntity.status(HttpStatus.OK).body(guias);
    }

    @PostMapping
    public ResponseEntity<String> criarGuiaDeEstudos(
        @RequestParam("autor") String autor,
        @RequestParam("titulo") String titulo,
        @RequestParam("conteudo") String conteudo,
        @RequestParam("regras") String regras,
        @RequestParam("links") List<String> links,
        @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
        @RequestParam("id_comite") Long id_comite) {
        
        GuiaEstudos guia = new GuiaEstudos();
        guia.setAutor(autor);
        guia.setTitulo(titulo);
        guia.setConteudo(conteudo);
        guia.setRegras(regras);
        List<LinkGuia> linksGuia = new ArrayList<>();
        for(String link : links) {
            linksGuia.add(new LinkGuia(link));
        }
        guia.setLinks(linksGuia);
        if(arquivo != null && !arquivo.isEmpty()) {
            try {
            guia.setArquivo(arquivo.getBytes());
            } catch(IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo");
            }
        } else {
            guia.setArquivo(null);
        }
        guia.setData(LocalDateTime.now());
        guia.setAtualizadoEm(LocalDateTime.now());
        guia.setOficial(true);
        guia.setAtivo(true);

        Long linhas = guiasService.criarGuia(guia);
        if (linhas == 1L) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Guia criado com sucesso");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar guia");
        }
    }

    @PostMapping("/{id}/atualizar")
    public ResponseEntity<String> atualizarGuiaDeEstudos(
        @PathVariable("id") Long id,
        @RequestParam("autor") String autor,
        @RequestParam("titulo") String titulo,
        @RequestParam("conteudo") String conteudo,
        @RequestParam("regras") String regras,
        @RequestParam("links") List<String> links,
        @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
        @RequestParam("id_comite") Long id_comite) {
        
        GuiaEstudos guia = new GuiaEstudos();
        guia.setAutor(autor);
        guia.setTitulo(titulo);
        guia.setConteudo(conteudo);
        guia.setRegras(regras);

        List<LinkGuia> linksGuia = new ArrayList<>();
        for(String link : links) {
            linksGuia.add(new LinkGuia(id, link));
        }
        guia.setLinks(linksGuia);

        if(arquivo != null && !arquivo.isEmpty()) {
            try {
            guia.setArquivo(arquivo.getBytes());
            } catch(IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar arquivo");
            }
        } else {
            guia.setArquivo(null);
        }

        guia.setData(LocalDateTime.now());
        guia.setAtualizadoEm(LocalDateTime.now());
        guia.setOficial(true);
        guia.setAtivo(true);

        int linhas = guiasService.atualizarPorId(id, guia);
        if (linhas == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("Guia atualizado com sucesso");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar guia");
        }
    }

    @GetMapping("/{id}/deletar")
    public ResponseEntity<String> getMethodName(@PathVariable Long id) {
        
        int linhas = guiasService.desativarPorId(id);

        if (linhas == 1) {
            return ResponseEntity.ok("Deletado com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao deletar guia");
        }
    }
    
}
