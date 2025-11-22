package com.mocs_on.controller;

import com.mocs_on.domain.Usuario;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.HDataSource;
import com.mocs_on.service.LoginDAO;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/comite")
public class ComiteController {

    private final LoginDAO loginDAO;
    private final AuthenticationManager authenticationManager;
    
    @Autowired
    public ComiteController(LoginDAO loginDAO, AuthenticationManager authenticationManager) {
        this.loginDAO = loginDAO;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/criar")
    public String criarComite(Model model, HttpSession session) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof SecaoUsuario) {
            SecaoUsuario secaoUsuario = (SecaoUsuario) principal;
            String cargoUsuario = secaoUsuario.getCargo().name();
        
            if (cargoUsuario.equals("VISITANTE") || cargoUsuario.equals("IMPRENSA") || cargoUsuario.equals("APOIADOR") || cargoUsuario.equals("EQUIPE")) {
                throw new Exception("Acesso negado");
            }

            //else {
            //    throw new Exception("Usuário não autenticado ou tipo inválido.");
            //}
        }

        return "criar_comite";
    }

    @PostMapping("/salvar")
    public String salvarComite(Model model, HttpSession session) {
        ComiteDao comiteDAO = new ComiteDAO(HDataSource.getSessionFactory());

        
        
        return "dashboard";
    }

    public String listarComites(Model model, HttpSession session) {
        return "dashboard";
    }

}
