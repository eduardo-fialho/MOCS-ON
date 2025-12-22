package com.mocs_on.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mocs_on.domain.RegistrationForm;

@Controller
@RequestMapping("/auth/register")
public class RegistrationController {

    @GetMapping
    public String showForm(Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        return "redirect:/admin/users/new";
    }

    @PostMapping
    public String handleSubmit(@ModelAttribute("form") RegistrationForm form,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        redirectAttributes.addFlashAttribute("info", "Fluxo de cadastro movido para a gestão de usuários.");
        return "redirect:/admin/users/new";
    }

    private boolean isSecretariat(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object role = session.getAttribute(AuthController.SESSION_USER_ROLE);
        return role != null && "SECRETARIADO".equalsIgnoreCase(role.toString());
    }
}
