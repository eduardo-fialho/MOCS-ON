package com.mocs_on.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mocs_on.service.CurtidaDAO;

@RestController
@RequestMapping("/post")
@CrossOrigin(origins = "*")
public class CurtidaController {

    @Autowired
    private CurtidaDAO curtidaDAO;

    @GetMapping("/{postId}/curtidas")
    public ResponseEntity<List<Map<String,String>>> listCurtidas(@PathVariable Long postId) {
        List<Map<String,String>> likes = curtidaDAO.getCurtidasForPost(postId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/{postId}/curtidas/count")
    public ResponseEntity<Map<String,Object>> countCurtidas(@PathVariable Long postId) {
        int cnt = curtidaDAO.countCurtidas(postId);
        Map<String,Object> out = new HashMap<>();
        out.put("count", cnt);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{postId}/curtida")
    public ResponseEntity<Void> toggleCurtida(
            @PathVariable Long postId,
            @RequestBody Map<String,String> body,
            Principal principal,
            Authentication authentication) {

        String usuario = null;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) usuario = principal.getName();
        else if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) usuario = authentication.getName();
        else usuario = body != null ? body.get("usuario") : null;

        if (usuario == null || usuario.isBlank()) return ResponseEntity.badRequest().build();

        String usuarioNome = null;
        if (body != null) usuarioNome = body.get("usuarioNome");

        boolean exists = curtidaDAO.hasCurtida(postId, usuario);
        if (exists) {
            int removed = curtidaDAO.removeCurtida(postId, usuario);
            if (removed > 0) return ResponseEntity.noContent().build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } else {
            Long id = curtidaDAO.addCurtida(postId, usuario, usuarioNome);
            if (id != null) return ResponseEntity.status(HttpStatus.CREATED).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
