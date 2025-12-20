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
            @RequestParam int mes,
            @RequestParam String tipo
    ) {
        return service.listarPorMes(ano, mes, tipo);
    }

    @GetMapping("/todos")
    public List<AgendaDiaria> listarTodos(@RequestParam String tipo) {
        return service.listarTodos(tipo);
    }

    @PostMapping("/criar")
    public ResponseEntity<String> criarEvento(@RequestBody AgendaDiaria dto) {
        if (dto.getTipo() == null) {
            dto.setTipo("GERAL");
        }
        service.salvar(dto);
        return ResponseEntity.ok("Evento criado com sucesso!");
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<String> editar(
            @PathVariable Long id,
            @RequestBody AgendaDiaria dados
    ) {
        if (dados.getVisivel() != null && !dados.getVisivel()) {
            service.atualizarVisibilidade(id, false);
            return ResponseEntity.ok("Evento excluído.");
        }

        service.editar(id, dados);
        return ResponseEntity.ok("Evento atualizado.");
    }
}
