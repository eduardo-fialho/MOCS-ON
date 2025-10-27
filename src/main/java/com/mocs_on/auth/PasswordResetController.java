package com.mocs_on.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "forgot_password_form"; // view com formulário de e-mail
    }

    @PostMapping(path = "/forgot-password", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public String forgotPassword(@RequestParam("email") String email,
                                 HttpServletRequest req,
                                 Model model) {
        String ip = req.getRemoteAddr();
        String ua = req.getHeader("User-Agent");
        // Sempre responder genericamente
        try {
            service.requestReset(email, ip, ua);
        } catch (Exception ignored) {
        }
        model.addAttribute("message", "Se o e-mail estiver cadastrado, enviaremos instruções para redefinição.");
        return "forgot_password_result"; // view simples com a mensagem
    }

    @GetMapping("/reset-password")
    public String showReset(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset_password_form"; // view com formulário
    }

    @PostMapping(path = "/reset-password", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String doReset(@RequestParam("token") String token,
                          @RequestParam("password") String password,
                          @RequestParam("confirm") String confirm,
                          Model model) {
        if (password == null || password.length() < 8) {
            model.addAttribute("error", "A senha deve ter pelo menos 8 caracteres.");
            model.addAttribute("token", token);
            return "reset_password_form";
        }
        if (!password.equals(confirm)) {
            model.addAttribute("error", "As senhas não coincidem.");
            model.addAttribute("token", token);
            return "reset_password_form";
        }

        boolean ok = false;
        try { ok = service.resetPassword(token, password); } catch (Exception ignored) {}
        if (!ok) {
            model.addAttribute("invalid", true);
            return "reset_password_invalid";
        }
        return "reset_password_success";
    }
}
