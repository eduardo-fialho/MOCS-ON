package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/auth/register")
public class RegistrationController {

    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.users.default-type:DELEGADO}")
    private String defaultUserType;

    public RegistrationController(UserAccountService userAccountService,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
    }

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

    private static final List<String> DEFAULT_ROLES = List.of(
            "SECRETARIADO",
            "EQUIPE",
            "DIRETOR",
            "IMPRENSA",
            "DELEGADO",
            "VISITANTE",
            "APOIADOR"
    );

    public static class RegistrationForm {
        private String name;
        private String email;
        private String password;
        private String confirmPassword;
        private String tipo;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }
    }
}
