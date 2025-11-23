package com.mocs_on.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import com.mocs_on.security.SecaoUsuario;

import jakarta.servlet.http.HttpSession;

@Controller
@CrossOrigin(origins = "*")
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        if (session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null) {
            return "redirect:/dashboard.html";
        }
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        return "login";
    }

    @GetMapping("/login.html")
    public String legacyLoginPath() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard.html")
    public String dashboard(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "dashboard";
    }

    @GetMapping("/mesa_diretora.html")
    public String mesaDiretora(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "mesa_diretora";
    }

    @GetMapping("/secretariado.html")
    public String secretariado(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "secretariado";
    }

    @GetMapping("/documentos.html")
    public String documentos(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "documentos";
    }

    @GetMapping("/avaliar_documentos.html")
    public String avaliar(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "avaliar_documentos";
    }

    @GetMapping("/submissao_documentos.html")
    public String submeterDocumento(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "submissao_documentos";
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null;
    }

    private boolean isAuthenticatedSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof SecaoUsuario;
    }

    private void populateUserAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario user) {
            model.addAttribute("usuarioNome", user.getNome());
            model.addAttribute("usuarioTipo", user.getCargo().name());
        } else {
            model.addAttribute("usuarioNome", "Visitante");
            model.addAttribute("usuarioTipo", "VISITANTE");
        }
    }
}
