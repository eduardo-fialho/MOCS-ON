package com.mocs_on.controller;

import com.mocs_on.domain.Materia;
import com.mocs_on.service.MateriaDAO;
import org.springframework.web.bind.annotation.*;

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
}

