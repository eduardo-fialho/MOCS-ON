package com.mocs_on.controller;

import com.mocs_on.auth.EmailService;
import com.mocs_on.auth.UserAccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserAccountService userAccountService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserManagementController(UserAccountService userAccountService,
                                    EmailService emailService,
                                    BCryptPasswordEncoder passwordEncoder) {
        this.userAccountService = userAccountService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listUsers(Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        model.addAttribute("users", userAccountService.findAllUsers());
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        return "admin_user_list";
    }

    @GetMapping("/{id}/edit")
    public String editUser(@PathVariable("id") long id,
                           Model model,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuário não encontrado.");
            return "redirect:/admin/users";
        }

        UserAccountService.UserRecord user = userOpt.get();
        UserEditForm form = new UserEditForm();
        form.setName(user.name());
        form.setEmail(user.email());
        form.setTipo(user.type());

        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        return "admin_user_edit";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable("id") long id,
                             @ModelAttribute("form") UserEditForm form,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        Optional<UserAccountService.UserRecord> existingOpt = userAccountService.findUserById(id);
        if (existingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Usuário não encontrado.");
            return "redirect:/admin/users";
        }

        UserAccountService.UserRecord existing = existingOpt.get();

        String name = form.getName() == null ? "" : form.getName().trim();
        String email = userAccountService.normalizeEmail(form.getEmail());
        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Informe o nome do usuário.");
            return String.format("redirect:/admin/users/%d/edit", id);
        }
        if (!userAccountService.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Informe um e-mail válido.");
            return String.format("redirect:/admin/users/%d/edit", id);
        }

        String tipo = form.getTipo() == null ? existing.type() : form.getTipo().trim().toUpperCase(Locale.ROOT);
        String passwordHash = null;
        boolean passwordReset = false;
        if (form.getNewPassword() != null && !form.getNewPassword().isBlank()) {
            if (form.getNewPassword().length() < 8) {
                redirectAttributes.addFlashAttribute("error", "A nova senha deve ter pelo menos 8 caracteres.");
                return String.format("redirect:/admin/users/%d/edit", id);
            }
            passwordHash = passwordEncoder.encode(form.getNewPassword());
            passwordReset = true;
        }

        try {
            String changedBy = session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE).toString();
            userAccountService.updateUser(id, name, email, tipo, passwordHash, changedBy);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return String.format("redirect:/admin/users/%d/edit", id);
        } catch (DataAccessException ex) {
            redirectAttributes.addFlashAttribute("error", "Erro ao salvar alterações. Tente novamente.");
            return String.format("redirect:/admin/users/%d/edit", id);
        }

        Optional<UserAccountService.UserRecord> updatedOpt = userAccountService.findUserById(id);
        if (updatedOpt.isPresent()) {
            sendProfileUpdateEmail(existing, updatedOpt.get(), passwordReset ? form.getNewPassword() : null);
        }

        redirectAttributes.addFlashAttribute("success", "Usuário atualizado com sucesso.");
        return "redirect:/admin/users";
    }

    private void sendProfileUpdateEmail(UserAccountService.UserRecord previous,
                                        UserAccountService.UserRecord updated,
                                        String newPasswordPlain) {
        String emailDestino = updated.email();
        StringBuilder body = new StringBuilder();
        body.append("Olá ").append(safe(updated.name())).append(",\n\n");
        body.append("O Secretariado atualizou o seu cadastro no MOCS ON em ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append(".\n");

        boolean anyChange = false;
        if (!safe(previous.name()).equals(updated.name())) {
            body.append("- Nome: ").append(safe(previous.name())).append(" → ").append(safe(updated.name())).append("\n");
            anyChange = true;
        }
        if (!previous.email().equalsIgnoreCase(updated.email())) {
            body.append("- E-mail: ").append(previous.email()).append(" → ").append(updated.email()).append("\n");
            anyChange = true;
        }
        if (!safe(previous.type()).equalsIgnoreCase(safe(updated.type()))) {
            body.append("- Papel: ").append(safe(previous.type())).append(" → ").append(safe(updated.type())).append("\n");
            anyChange = true;
        }
        if (newPasswordPlain != null) {
            body.append("- Senha redefinida. Nova senha temporária: ").append(newPasswordPlain).append("\n");
            anyChange = true;
        }

        if (!anyChange) {
            return;
        }

        body.append("\nSe você não reconhece esta alteração, procure o Secretariado imediatamente.\n\n");
        body.append("Equipe MOCS ON\n");

        emailService.send(emailDestino, "MOCS ON - Atualização do seu cadastro", body.toString());
    }

    private boolean isSecretariat(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object role = session.getAttribute(AuthController.SESSION_USER_ROLE);
        return role != null && "SECRETARIADO".equalsIgnoreCase(role.toString());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static final List<String> ROLE_OPTIONS = List.of(
            "SECRETARIADO",
            "EQUIPE",
            "DIRETOR",
            "IMPRENSA",
            "DELEGADO",
            "VISITANTE",
            "APOIADOR"
    );

    public static class UserEditForm {
        private String name;
        private String email;
        private String tipo;
        private String newPassword;

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

        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
