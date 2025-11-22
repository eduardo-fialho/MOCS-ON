package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;

    public ProfileController(UserAccountService userAccountService,
                             BCryptPasswordEncoder passwordEncoder) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showProfile(Model model,
                              HttpSession session) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord user = userOpt.get();

        if (!model.containsAttribute("profileForm")) {
            ProfileForm form = new ProfileForm();
            form.setName(user.name());
            form.setEmail(user.email());
            model.addAttribute("profileForm", form);
        }

        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordForm());
        }

        List<UserAccountService.UserChangeLogRecord> changes = userAccountService.findChangeLogsByUserId(userId);
        int limit = Math.min(changes.size(), 10);
        model.addAttribute("changes", changes.subList(0, limit));
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/details")
    public String updateDetails(@ModelAttribute("profileForm") ProfileForm form,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord existing = userOpt.get();

        String name = form.getName() == null ? "" : form.getName().trim();
        String email = userAccountService.normalizeEmail(form.getEmail());

        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("profileError", "Informe o seu nome.");
            redirectAttributes.addFlashAttribute("profileForm", form);
            return "redirect:/profile";
        }
        if (!userAccountService.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("profileError", "Informe um e-mail válido.");
            redirectAttributes.addFlashAttribute("profileForm", form);
            return "redirect:/profile";
        }

        try {
            userAccountService.updateUser(
                    userId,
                    name,
                    email,
                    existing.type(),
                    null,
                    existing.email()
            );
        } catch (IllegalArgumentException | DataAccessException ex) {
            redirectAttributes.addFlashAttribute("profileError", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileForm", form);
            return "redirect:/profile";
        }

        session.setAttribute(AuthController.SESSION_USER_NAME, name);
        session.setAttribute(AuthController.SESSION_USER_ATTRIBUTE, email);
        redirectAttributes.addFlashAttribute("profileSuccess", "Dados atualizados com sucesso.");
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String updatePassword(@ModelAttribute("passwordForm") PasswordForm form,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = currentUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }
        UserAccountService.UserRecord existing = userOpt.get();

        if (form.getCurrentPassword() == null || form.getCurrentPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("passwordError", "Informe a senha atual.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return "redirect:/profile";
        }
        if (!passwordEncoder.matches(form.getCurrentPassword(), existing.passwordHash())) {
            redirectAttributes.addFlashAttribute("passwordError", "Senha atual incorreta.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return "redirect:/profile";
        }
        if (form.getNewPassword() == null || form.getNewPassword().length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "A nova senha deve ter pelo menos 8 caracteres.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return "redirect:/profile";
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "As novas senhas precisam ser iguais.");
            redirectAttributes.addFlashAttribute("passwordForm", form);
            return "redirect:/profile";
        }

        String hash = passwordEncoder.encode(form.getNewPassword());
        userAccountService.updateUser(
                userId,
                existing.name(),
                existing.email(),
                existing.type(),
                hash,
                existing.email()
        );
        redirectAttributes.addFlashAttribute("passwordSuccess", "Senha atualizada com sucesso.");
        return "redirect:/profile";
    }

    private Long currentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object idAttr = session.getAttribute(AuthController.SESSION_USER_ID);
        if (idAttr instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    public static class ProfileForm {
        private String name;
        private String email;

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
    }

    public static class PasswordForm {
        private String currentPassword;
        private String newPassword;
        private String confirmPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
