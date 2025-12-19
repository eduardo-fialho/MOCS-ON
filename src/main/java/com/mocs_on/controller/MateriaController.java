package com.mocs_on.controller;

import com.mocs_on.domain.Materia;
import com.mocs_on.domain.StatusMateria;
import com.mocs_on.service.MateriaService;
import com.mocs_on.security.SecaoUsuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return "imprensa/feed";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("materia", new Materia());
        return "nova_materia";
    }

    @PostMapping
    public String criar(
            @RequestParam String titulo,
            @RequestParam String lead,
            @RequestParam String texto,
            @RequestParam(required = false) MultipartFile imagem
    ) {
        String usuario = usuarioLogado();

        Materia materia = new Materia();
        materia.setTitulo(titulo);
        materia.setLead(lead);
        materia.setTexto(texto);
        materia.setAutor(usuario);
        materia.setStatus(StatusMateria.PENDENTE);
        materia.setAtivo(true);

        if (imagem != null && !imagem.isEmpty()) {
            try {
                materia.setImagem(imagem.getBytes());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar imagem", e);
            }
        }

        materiaService.criar(materia, usuario);

        return "redirect:/imprensa";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("materia", materiaService.buscarPorId(id));
        return "editar_materia";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @RequestParam String titulo,
            @RequestParam String lead,
            @RequestParam String texto,
            @RequestParam(required = false) MultipartFile imagem
    ) {
        String usuario = usuarioLogado();

        Materia materia = materiaService.buscarPorId(id);

        materia.setTitulo(titulo);
        materia.setLead(lead);
        materia.setTexto(texto);

        materia.setStatus(StatusMateria.PENDENTE);

        if (imagem != null && !imagem.isEmpty()) {
            try {
                materia.setImagem(imagem.getBytes());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar imagem", e);
            }
        }

        materiaService.atualizar(materia, usuario);

        return "redirect:/imprensa";
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

    @PostMapping("/{id}/arquivar")
    public String arquivar(@PathVariable Long id) {

        materiaService.arquivar(id, usuarioLogado());
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
