
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

import com.mocs_on.domain.Post;
import com.mocs_on.domain.SpottedPost;
import com.mocs_on.service.SpottedDAO;

@RestController
@RequestMapping("/spotteds")
@CrossOrigin(origins = "*")
public class SpottedsController {
    
    @Autowired
    private SpottedDAO spottedService;

    @GetMapping
    public ResponseEntity<List<SpottedPost>> recuperarAviso() {
        List<SpottedPost> avisos = spottedService.recuperarTodos();
        return ResponseEntity.ok(avisos);
    }
    
    @PostMapping
    public ResponseEntity<Void> postarAviso(@RequestBody SpottedPost spotted) {
        spotted.setDataPublicacao(LocalDateTime.now());
        int linhasAfetadas = spottedService.inserirAviso(spotted);
        if (linhasAfetadas == 1) return ResponseEntity.status(HttpStatus.CREATED).build();
        else return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
