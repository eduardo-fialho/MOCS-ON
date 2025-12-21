package com.mocs_on.controller;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mocs_on.domain.Consulta;
import com.mocs_on.service.ConsultaService;


@RestController
@RequestMapping("/api/consultas")
public class ConsultaApiController {

    private final ConsultaService consultaService;

    public ConsultaApiController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public List<Consulta> listar() {
        return consultaService.listarTodas();
    }

    @GetMapping("/{id}")
    public Consulta buscarPorId(@PathVariable Long id) {
        return consultaService.buscarPorId(id);
    }
}
