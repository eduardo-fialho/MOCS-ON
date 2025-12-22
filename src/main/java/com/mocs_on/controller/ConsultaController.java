package com.mocs_on.controller;

import com.mocs_on.domain.Consulta;
import com.mocs_on.domain.StatusConsulta;
import com.mocs_on.service.ConsultaService;
import com.mocs_on.security.SecaoUsuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("todas", consultaService.listarTodas());
        model.addAttribute("pendentes", consultaService.listarPorStatus(StatusConsulta.PENDENTE));
        model.addAttribute("aprovadas", consultaService.listarPorStatus(StatusConsulta.APROVADA));
        return "consultas";
    }


    @GetMapping("/pendentes")
    public String listarPendentes(Model model) {
        model.addAttribute(
            "consultas",
            consultaService.listarPorStatus(StatusConsulta.PENDENTE)
        );
        return "consultas";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("consulta", new Consulta());
        return "nova_consulta";
    }

    @GetMapping("/feed")
    public String feed(Model model) {
        model.addAttribute("todas", consultaService.listarTodas());
        model.addAttribute("pendentes", consultaService.listarPorStatus(StatusConsulta.PENDENTE));
        model.addAttribute("aprovadas", consultaService.listarPorStatus(StatusConsulta.APROVADA));

        return "consultas_feed";
    }

    @PostMapping
    public String criar(
            @RequestParam String titulo,
            @RequestParam String pergunta
    ) {
        Consulta consulta = new Consulta();
        consulta.setTitulo(titulo);
        consulta.setPergunta(pergunta);

        consultaService.criar(consulta);

        return "redirect:/consultas";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("consulta", consultaService.buscarPorId(id));
        return "consultas";
    }

    //Pus esse endpoint bizarro pra ver se funciona
    @GetMapping("api/consultas/{id}")
    @ResponseBody
    public Consulta apiConsultas(@PathVariable Long id, Model model) {
        return consultaService.buscarPorId(id);
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(@PathVariable Long id) {
        consultaService.aprovar(id);
        return "redirect:/consultas";
    }

    @PostMapping("/{id}/rejeitar")
    public String rejeitar(@PathVariable Long id) {
        consultaService.rejeitar(id);
        return "redirect:/consultas";
    }

    @PostMapping("/{id}/arquivar")
    public String arquivar(@PathVariable Long id) {
        consultaService.arquivar(id);
        return "redirect:/consultas";
    }

    private String usuarioLogado() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        Object principal = auth.getPrincipal();

        if (principal instanceof SecaoUsuario secaoUsuario) {
            return secaoUsuario.getUsername();
        }

        return "sistema";
    }
}
