package com.mocs_on.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mocs_on.domain.RelatoOuvidoria;
import com.mocs_on.domain.StatusRelatoOuvidoria;
import com.mocs_on.service.RelatoOuvidoriaDAO;

@RestController
@RequestMapping("/ouvidoria")
public class OuvidoriaController {

    @Autowired
    private RelatoOuvidoriaDAO ouvidoriaService;

    @GetMapping
    public ResponseEntity<List<RelatoOuvidoria>> listarTodos() {
        return ResponseEntity.ok(ouvidoriaService.recuperarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelatoOuvidoria> recuperarPorId(@PathVariable Long id) {
        RelatoOuvidoria relato = ouvidoriaService.recuperarPorId(id);
        if (relato != null) {
            return ResponseEntity.ok(relato);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping
    public ResponseEntity<String> criarRelato(
            @RequestParam String autor,
            @RequestParam String assunto,
            @RequestParam String relato) {

        RelatoOuvidoria r = new RelatoOuvidoria();
        r.setAutor(autor);
        r.setAssunto(assunto);
        r.setRelato(relato);
        r.setCriadoEm(LocalDateTime.now());
        r.setStatus(StatusRelatoOuvidoria.ABERTO);
        r.setAtivo(true);

        Long id = ouvidoriaService.criar(r);
        if (id != null) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Relato criado com sucesso");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar relato");
    }

    @PostMapping("/{id}/responder")
    public ResponseEntity<String> responderRelato(
            @PathVariable Long id,
            @RequestParam String ouvidor,
            @RequestParam String resposta,
            @RequestParam StatusRelatoOuvidoria status) {

        int linhas = ouvidoriaService.responder(id, ouvidor, resposta, status);
        if (linhas == 1) {
            return ResponseEntity.ok("Relato respondido com sucesso");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao responder relato");
    }

    @GetMapping("/{id}/deletar")
    public ResponseEntity<String> deletar(@PathVariable Long id) {
        int linhas = ouvidoriaService.desativar(id);
        if (linhas == 1) {
            return ResponseEntity.ok("Relato desativado com sucesso");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao desativar relato");
    }
}
