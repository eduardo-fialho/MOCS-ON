package com.mocs_on.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mocs_on.domain.Aviso;
import com.mocs_on.service.AvisoDAO;

@RestController
@RequestMapping("/aviso")
@CrossOrigin(origins = "*")
public class AvisoController {
    
    @Autowired
    private AvisoDAO avisoService;

    @GetMapping
    public ResponseEntity<List<Aviso>> recuperarAviso() {
        List<Aviso> avisos = avisoService.recuperarTodos();
        return ResponseEntity.ok(avisos);
    }
    
    @PostMapping
    public ResponseEntity<Void> postarAviso(@RequestBody Aviso aviso) {
        aviso.setData(LocalDateTime.now());
        int linhasAfetadas = avisoService.inserirAviso(aviso);
        if (linhasAfetadas == 1) return ResponseEntity.status(HttpStatus.CREATED).build();
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/quantidade")
    public ResponseEntity<Integer> quantidadeAvisos() {
        return ResponseEntity.ok(avisoService.quantidadeAvisos());
    }
}
