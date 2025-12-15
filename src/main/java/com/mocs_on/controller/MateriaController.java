package com.mocs_on.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mocs_on.domain.Materia;
import com.mocs_on.domain.StatusMateria;
import com.mocs_on.service.MateriaDAO;

@RestController
@RequestMapping("/materias")
@CrossOrigin(origins = "*")
public class MateriaController {

    @Autowired
    private MateriaDAO materiaDAO;

    @PostMapping
    public ResponseEntity<String> criar(
            @RequestParam String titulo,
            @RequestParam String lead,
            @RequestParam String texto,
            @RequestParam String autor,
            @RequestParam(required = false) Long comiteId,
            @RequestParam(required = false) MultipartFile imagem
    ) throws IOException {

        Materia m = new Materia();
        m.setTitulo(titulo);
        m.setLead(lead);
        m.setTexto(texto);
        m.setAutor(autor);
        m.setComiteId(comiteId);
        m.setStatus(StatusMateria.PENDENTE);
        m.setAtivo(true);

        if (imagem != null && !imagem.isEmpty()) {
            m.setImagem(imagem.getBytes());
        }

        materiaDAO.inserir(m);
        return ResponseEntity.ok("Matéria criada com sucesso");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> editar(
            @PathVariable Long id,
            @RequestBody Materia dados,
            @RequestParam String usuarioLogado
    ) {
        Materia m = materiaDAO.buscarPorId(id);

        if (m == null) {
            return ResponseEntity.notFound().build();
        }

        if (!m.getAutor().equals(usuarioLogado)) {
            return ResponseEntity.status(403).body("Apenas o autor pode editar");
        }

        m.setTitulo(dados.getTitulo());
        m.setLead(dados.getLead());
        m.setTexto(dados.getTexto());
        m.setImagem(dados.getImagem());

        materiaDAO.atualizar(m);
        return ResponseEntity.ok("Matéria editada");
    }

    @GetMapping("/pendentes")
    public List<Materia> pendentes() {
        return materiaDAO.listarPendentes();
    }

    @PostMapping("/{id}/avaliar")
    public ResponseEntity<String> avaliar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestParam String revisor
    ) {
        StatusMateria status = StatusMateria.valueOf(body.get("status"));
        materiaDAO.avaliar(id, status, revisor);
        return ResponseEntity.ok("Matéria avaliada");
    }
}
