package com.mocs_on.controller;

import com.mocs_on.auth.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @GetMapping("/forgot-password")
    public String showForgotForm(Model model) {
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        return "forgot_password_form";
    }

    @PostMapping("/forgot-password")
    public String submitForgot(@RequestParam("email") String email,
                               HttpServletRequest request,
                               Model model) {
        service.requestReset(email, request.getRemoteAddr(), request.getHeader("User-Agent"));
        model.addAttribute("message", "Se o e-mail estiver cadastrado, enviamos instrucoes para redefinicao.");
        return "forgot_password_result";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset_password_form";
    }

    @PostMapping("/reset-password")
    public String submitReset(@RequestParam("token") String token,
                              @RequestParam("password") String password,
                              @RequestParam("confirm") String confirm,
                              Model model) {

        if (password == null || password.length() < 8) {
            model.addAttribute("error", "A senha deve ter pelo menos 8 caracteres.");
            model.addAttribute("token", token);
            return "reset_password_form";
        }

        if (!password.equals(confirm)) {
            model.addAttribute("error", "As senhas nao coincidem.");
            model.addAttribute("token", token);
            return "reset_password_form";
        }

        PasswordResetService.ResetPasswordStatus status = service.resetPassword(token, password);
        switch (status) {
            case SUCCESS:
                return "reset_password_success";
            case SAME_PASSWORD:
                model.addAttribute("error", "Escreva uma senha diferente da anterior.");
                model.addAttribute("token", token);
                return "reset_password_form";
            default:
                return "reset_password_invalid";
        }
    }
}
