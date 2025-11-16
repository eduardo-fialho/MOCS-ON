package com.mocs_on.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import com.mocs_on.security.SecaoUsuario;

@Controller
@CrossOrigin(origins = "*")
public class HomeController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login.html") 
    public String login(){
        return "login";
    }

    @GetMapping("/dashboard.html") 
    public String dashBoard(Model model){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        String nomeUsuario = "Visitante";
        String tipoUsuario = "VISITANTE"; 

        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario) {
            SecaoUsuario user = (SecaoUsuario) authentication.getPrincipal();

            nomeUsuario = user.getNome();
            tipoUsuario = user.getCargo().name();
        }
        
        model.addAttribute("usuarioNome", nomeUsuario);
        model.addAttribute("usuarioTipo", tipoUsuario);
        
        return "dashboard";
    }

    @GetMapping("/mesa_diretora.html")
    public String mesaDiretora() {
        return "mesa_diretora";
    }

    @GetMapping("/secretariado.html")
    public String secretariado(Model model) { 
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        String nomeUsuario = "Secretário Desconhecido"; 
        String tipoUsuario = "DESCONHECIDO"; 

        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario) {
            SecaoUsuario user = (SecaoUsuario) authentication.getPrincipal();

            nomeUsuario = user.getNome();
            tipoUsuario = user.getCargo().name();
        }
        
        model.addAttribute("usuarioNome", nomeUsuario);
        model.addAttribute("usuarioTipo", tipoUsuario);
        
        return "secretariado";
    }
}
