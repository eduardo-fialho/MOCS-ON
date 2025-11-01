package com.mocs_on.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
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
    public String dashboard(HttpSession session) {
        return isAuthenticated(session) ? "dashboard" : "redirect:/login";
    }

    @GetMapping("/mesa_diretora.html")
    public String mesaDiretora(HttpSession session) {
        return isAuthenticated(session) ? "mesa_diretora.html" : "redirect:/login";
    }

    @GetMapping("/secretariado.html")
    public String secretariado(HttpSession session) {
        return isAuthenticated(session) ? "secretariado.html" : "redirect:/login";
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null;
    }
}
