package com.mocs_on.controller;

import com.mocs_on.domain.Materia;
import com.mocs_on.service.MateriaDAO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/imprensa/materias")
public class ImprensaRestController {

    private final MateriaDAO materiaDAO;

    public ImprensaRestController(MateriaDAO materiaDAO) {
        this.materiaDAO = materiaDAO;
    }

    @GetMapping
    public List<Materia> listarMateriasPublicas() {
        return materiaDAO.listar();
    }

    @GetMapping("/{id}")
    public Materia buscar(@PathVariable Long id) {
        return materiaDAO.buscarPorId(id);
    }

    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> imagem(@PathVariable Long id) {

        Materia materia = materiaDAO.buscarPorId(id);

        if (materia == null || materia.getImagem() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE
)
            .body(materia.getImagem());
    }
}

