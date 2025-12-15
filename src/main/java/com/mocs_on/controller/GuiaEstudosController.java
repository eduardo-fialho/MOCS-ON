package com.mocs_on.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.mocs_on.domain.GuiaEstudos;
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
    public ResponseEntity<String> postMethodName(@RequestBody GuiaEstudos guia) {
        
        return ResponseEntity.status(HttpStatus.CREATED).body("Guia criado com sucesso!");
    }
    
}
