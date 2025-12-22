package com.mocs_on.controller;

import com.mocs_on.domain.TipoVoto;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.ConsultaService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/consultas")
public class ConsultaVotoController {

    private final ConsultaService consultaService;

    public ConsultaVotoController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping("/{id}/votar")
    public String votar(
            @PathVariable Long id,
            @RequestParam TipoVoto voto
    ) {
        String usuarioName = usuarioLogado();

        consultaService.votar(id, usuarioName, voto);

        return "redirect:/consultas/" + id;
    }

    @GetMapping("/{id}/votos")
    public Map<String, Integer> listarVotos(@PathVariable Long id) {
        return consultaService.contarVotosTotais(id);
    }

    private String usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof SecaoUsuario secaoUsuario) {
            return secaoUsuario.getUsername(); 
        }

        return "sistema";
    }
}
