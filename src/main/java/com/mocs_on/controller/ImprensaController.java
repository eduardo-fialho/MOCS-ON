package com.mocs_on.controller;

import com.mocs_on.domain.Materia;
import com.mocs_on.service.MateriaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/imprensa")
public class ImprensaController {

    private final MateriaService materiaService;

    public ImprensaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public String paginaImprensa() {
        return "imprensa"; 
    }
}
