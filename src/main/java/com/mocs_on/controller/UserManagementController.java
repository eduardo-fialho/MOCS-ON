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
        model.addAttribute("secretariadoFuncaoOptions", SECRETARIADO_FUNCOES);
        return "admin_user_list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        if (!model.containsAttribute("form")) {
            UserCreateForm form = new UserCreateForm();
            form.setTipo("DELEGADO");
            model.addAttribute("form", form);
        }
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("secretariadoFuncaoOptions", SECRETARIADO_FUNCOES);
        return "admin_user_create";
    }

    @PostMapping("/new")
    public String handleCreate(@ModelAttribute("form") UserCreateForm form,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }

        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("secretariadoFuncaoOptions", SECRETARIADO_FUNCOES);

        String name = form.getName() == null ? "" : form.getName().trim();
        String email = userAccountService.normalizeEmail(form.getEmail());

        if (name.isBlank()) {
            model.addAttribute("error", "Informe o nome completo do usuário.");
            return "admin_user_create";
        }
        if (!userAccountService.isValidEmail(email)) {
            model.addAttribute("error", "Informe um e-mail válido.");
            return "admin_user_create";
        }
        if (form.getPassword() == null || form.getPassword().length() < 8) {
            model.addAttribute("error", "A senha deve ter pelo menos 8 caracteres.");
            return "admin_user_create";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "As senhas precisam ser iguais.");
            return "admin_user_create";
        }
        if (userAccountService.userExists(email)) {
            model.addAttribute("error", "Este e-mail já está cadastrado.");
            return "admin_user_create";
        }

        String tipo = form.getTipo() == null || form.getTipo().isBlank() ? "DELEGADO" : form.getTipo().trim();
        if ("SECRETARIADO".equalsIgnoreCase(tipo)) {
            if (form.getSecretariadoFuncao() == null || form.getSecretariadoFuncao().isBlank()
                    || form.getSecretariadoDepartamento() == null || form.getSecretariadoDepartamento().isBlank()
                    || form.getSecretariadoResponsabilidades() == null || form.getSecretariadoResponsabilidades().isBlank()) {
                model.addAttribute("error", "Informe função, departamento e responsabilidades para o Secretariado.");
                return "admin_user_create";
            }
        }
        String hash = passwordEncoder.encode(form.getPassword());

        try {
            userAccountService.createUser(name, email, hash, tipo);
        } catch (DataAccessException ex) {
            model.addAttribute("error", "Não foi possível salvar os dados. Tente novamente.");
            return "admin_user_create";
        }

        Optional<UserAccountService.UserRecord> createdOpt = userAccountService.findUserByEmail(email);
        if (createdOpt.isPresent() && "SECRETARIADO".equalsIgnoreCase(tipo)) {
            userAccountService.upsertSecretariadoProfile(createdOpt.get().id(),
                    new UserAccountService.SecretariadoProfile(
                            form.getSecretariadoFuncao().trim().toUpperCase(Locale.ROOT),
                            form.getSecretariadoDepartamento().trim(),
                            safe(form.getSecretariadoMatricula()),
                            safe(form.getSecretariadoTelefone()),
                            safe(form.getSecretariadoTurno()),
                            safe(form.getSecretariadoResponsabilidades())
                    ));
        }

        sendWelcomeEmail(name, email, tipo, form.getPassword());

        redirectAttributes.addFlashAttribute("success", "Usuário criado com sucesso!");
        return "redirect:/admin/users";
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

        if ("SECRETARIADO".equalsIgnoreCase(user.type())) {
            userAccountService.findSecretariadoProfile(user.id()).ifPresent(profile -> {
                form.setSecretariadoFuncao(profile.funcao());
                form.setSecretariadoDepartamento(profile.departamento());
                form.setSecretariadoMatricula(profile.matricula());
                form.setSecretariadoTelefone(profile.telefone());
                form.setSecretariadoTurno(profile.turnoAtendimento());
                form.setSecretariadoResponsabilidades(profile.responsabilidades());
            });
        }

        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("secretariadoFuncaoOptions", SECRETARIADO_FUNCOES);
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
        if ("SECRETARIADO".equalsIgnoreCase(tipo)) {
            if (form.getSecretariadoFuncao() == null || form.getSecretariadoFuncao().isBlank()
                    || form.getSecretariadoDepartamento() == null || form.getSecretariadoDepartamento().isBlank()
                    || form.getSecretariadoResponsabilidades() == null || form.getSecretariadoResponsabilidades().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Informe função, departamento e responsabilidades para o Secretariado.");
                return String.format("redirect:/admin/users/%d/edit", id);
            }
        }
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
            UserAccountService.UserRecord updated = updatedOpt.get();
            if ("SECRETARIADO".equalsIgnoreCase(updated.type())) {
                userAccountService.upsertSecretariadoProfile(updated.id(),
                        new UserAccountService.SecretariadoProfile(
                                form.getSecretariadoFuncao().trim().toUpperCase(Locale.ROOT),
                                form.getSecretariadoDepartamento().trim(),
                                safe(form.getSecretariadoMatricula()),
                                safe(form.getSecretariadoTelefone()),
                                safe(form.getSecretariadoTurno()),
                                safe(form.getSecretariadoResponsabilidades())
                        ));
            } else {
                userAccountService.deleteSecretariadoProfile(updated.id());
            }
            sendProfileUpdateEmail(existing, updated, passwordReset ? form.getNewPassword() : null);
        }

        redirectAttributes.addFlashAttribute("success", "Usuário atualizado com sucesso.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/history")
    public String viewHistory(@PathVariable("id") long id,
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

        model.addAttribute("user", userOpt.get());
        model.addAttribute("changes", userAccountService.findChangeLogsByUserId(id));
        return "admin_user_history";
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

    private void sendWelcomeEmail(String name, String email, String tipo, String plainPassword) {
        StringBuilder body = new StringBuilder();
        body.append("Olá ").append(safe(name)).append(",\n\n");
        body.append("Seu acesso ao portal MOCS ON foi criado pelo Secretariado.\n");
        body.append("Dados de acesso:\n");
        body.append("- E-mail: ").append(email).append("\n");
        body.append("- Papel inicial: ").append(safe(tipo)).append("\n");
        body.append("- Senha provisória: ").append(plainPassword).append("\n\n");
        body.append("Ao entrar, recomendamos alterar a senha na primeira oportunidade.\n\n");
        body.append("Equipe MOCS ON\n");

        emailService.send(email, "MOCS ON - Bem-vindo(a)!", body.toString());
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
            "DELEGADO"
    );

    private static final List<String> SECRETARIADO_FUNCOES = List.of(
            "DOCENTE",
            "TECNICO_ADMINISTRATIVO"
    );

    public static class UserCreateForm {
        private String name;
        private String email;
        private String password;
        private String confirmPassword;
        private String tipo;
        private String secretariadoFuncao;
        private String secretariadoDepartamento;
        private String secretariadoMatricula;
        private String secretariadoTelefone;
        private String secretariadoTurno;
        private String secretariadoResponsabilidades;

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

        public String getSecretariadoFuncao() {
            return secretariadoFuncao;
        }

        public void setSecretariadoFuncao(String secretariadoFuncao) {
            this.secretariadoFuncao = secretariadoFuncao;
        }

        public String getSecretariadoDepartamento() {
            return secretariadoDepartamento;
        }

        public void setSecretariadoDepartamento(String secretariadoDepartamento) {
            this.secretariadoDepartamento = secretariadoDepartamento;
        }

        public String getSecretariadoMatricula() {
            return secretariadoMatricula;
        }

        public void setSecretariadoMatricula(String secretariadoMatricula) {
            this.secretariadoMatricula = secretariadoMatricula;
        }

        public String getSecretariadoTelefone() {
            return secretariadoTelefone;
        }

        public void setSecretariadoTelefone(String secretariadoTelefone) {
            this.secretariadoTelefone = secretariadoTelefone;
        }

        public String getSecretariadoTurno() {
            return secretariadoTurno;
        }

        public void setSecretariadoTurno(String secretariadoTurno) {
            this.secretariadoTurno = secretariadoTurno;
        }

        public String getSecretariadoResponsabilidades() {
            return secretariadoResponsabilidades;
        }

        public void setSecretariadoResponsabilidades(String secretariadoResponsabilidades) {
            this.secretariadoResponsabilidades = secretariadoResponsabilidades;
        }
    }

    public static class UserEditForm {
        private String name;
        private String email;
        private String tipo;
        private String newPassword;
        private String secretariadoFuncao;
        private String secretariadoDepartamento;
        private String secretariadoMatricula;
        private String secretariadoTelefone;
        private String secretariadoTurno;
        private String secretariadoResponsabilidades;

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

        public String getSecretariadoFuncao() {
            return secretariadoFuncao;
        }

        public void setSecretariadoFuncao(String secretariadoFuncao) {
            this.secretariadoFuncao = secretariadoFuncao;
        }

        public String getSecretariadoDepartamento() {
            return secretariadoDepartamento;
        }

        public void setSecretariadoDepartamento(String secretariadoDepartamento) {
            this.secretariadoDepartamento = secretariadoDepartamento;
        }

        public String getSecretariadoMatricula() {
            return secretariadoMatricula;
        }

        public void setSecretariadoMatricula(String secretariadoMatricula) {
            this.secretariadoMatricula = secretariadoMatricula;
        }

        public String getSecretariadoTelefone() {
            return secretariadoTelefone;
        }

        public void setSecretariadoTelefone(String secretariadoTelefone) {
            this.secretariadoTelefone = secretariadoTelefone;
        }

        public String getSecretariadoTurno() {
            return secretariadoTurno;
        }

        public void setSecretariadoTurno(String secretariadoTurno) {
            this.secretariadoTurno = secretariadoTurno;
        }

        public String getSecretariadoResponsabilidades() {
            return secretariadoResponsabilidades;
        }

        public void setSecretariadoResponsabilidades(String secretariadoResponsabilidades) {
            this.secretariadoResponsabilidades = secretariadoResponsabilidades;
        }
    }
}
