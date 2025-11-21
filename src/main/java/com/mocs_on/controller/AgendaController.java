package com.mocs_on.controller;

import com.mocs_on.domain.AgendaDiaria;
import com.mocs_on.service.AgendaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agenda")
public class AgendaController {

    private final AgendaService service;

    public AgendaController(AgendaService service) {
        this.service = service;
    }

    @GetMapping("/eventos")
    public List<AgendaDiaria> listarEventos(
            @RequestParam int ano,
            @RequestParam int mes) {
        return service.listarPorMes(ano, mes);
    }

    @PostMapping("/criar")
    public ResponseEntity<String> criarEvento(@RequestBody AgendaDiaria dto) {
        service.salvar(dto);
        return ResponseEntity.ok("Evento criado com sucesso!");
    }

    @PostMapping("/salvar")
    public void salvar(@RequestBody AgendaDiaria agenda) {
        service.salvar(agenda);
    }
}
