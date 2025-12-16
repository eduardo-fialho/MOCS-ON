package com.mocs_on.controller;

import com.mocs_on.domain.Materia;
import com.mocs_on.service.MateriaService;
import com.mocs_on.security.SecaoUsuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("materias", materiaService.listarTodas());
        return "materias/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("materia", new Materia());
        return "materias/form";
    }

    @PostMapping
    public String criar(@ModelAttribute Materia materia) {
        String usuario = usuarioLogado();
        materiaService.criar(materia, usuario);
        return "redirect:/materias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("materia", materiaService.buscarPorId(id));
        return "materias/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Materia materia) {
        String usuario = usuarioLogado();
        materia.setId(id);
        materiaService.atualizar(materia, usuario);
        return "redirect:/materias";
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(@PathVariable Long id) {
        materiaService.aprovar(id, usuarioLogado());
        return "redirect:/materias";
    }

    @PostMapping("/{id}/rejeitar")
    public String rejeitar(
            @PathVariable Long id,
            @RequestParam String motivo) {

        materiaService.rejeitar(id, motivo, usuarioLogado());
        return "redirect:/materias";
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
