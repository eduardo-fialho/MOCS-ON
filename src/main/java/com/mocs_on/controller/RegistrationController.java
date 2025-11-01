package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
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
    public String showForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrationForm());
        }
        return "register_form";
    }

    @PostMapping
    public String handleSubmit(@ModelAttribute("form") RegistrationForm form,
                               RedirectAttributes redirectAttributes) {
        String name = form.name == null ? "" : form.name.trim();
        String email = userAccountService.normalizeEmail(form.email);

        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "Informe o seu nome completo.");
            return "redirect:/auth/register";
        }
        if (!userAccountService.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "Informe um e-mail valido.");
            return "redirect:/auth/register";
        }
        if (form.password == null || form.password.length() < 8) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "A senha deve ter pelo menos 8 caracteres.");
            return "redirect:/auth/register";
        }
        if (!form.password.equals(form.confirmPassword)) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "As senhas precisam ser iguais.");
            return "redirect:/auth/register";
        }
        if (userAccountService.userExists(email)) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "Este e-mail ja esta cadastrado.");
            return "redirect:/auth/register";
        }

        String tipo = form.tipo == null || form.tipo.isBlank() ? defaultUserType : form.tipo.trim();
        String hash = passwordEncoder.encode(form.password);

        try {
            userAccountService.createUser(name, email, hash, tipo);
        } catch (DataAccessException ex) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "Nao foi possivel salvar os dados. Tente novamente.");
            return "redirect:/auth/register";
        }

        redirectAttributes.addFlashAttribute("success", "Conta criada com sucesso! Pode entrar com sua nova senha.");
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/login";
    }

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
