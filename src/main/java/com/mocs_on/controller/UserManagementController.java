package com.mocs_on.controller;

import com.mocs_on.auth.EmailService;
import com.mocs_on.auth.UserAccountService;
import com.mocs_on.domain.PreRegistration;
import jákarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jáva.time.LocalDateTime;
import jáva.time.format.DateTimeFormatter;
import jáva.util.List;
import jáva.util.Locale;
import jáva.util.Optional;

import com.mocs_on.service.PreRegistrationService;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserAccountService userAccountService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PreRegistrationService preRegistrationService;

    public UserManagementController(UserAccountService userAccountService,
                                    EmailService emailService,
                                    BCryptPasswordEncoder passwordEncoder,
                                    PreRegistrationService preRegistrationService) {
        this.userAccountService = userAccountService;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.preRegistrationService = preRegistrationService;
    }

    @PostMapping("/pre-registrations/{id}/deny")
    public String denyPreRegistration(@PathVariable("id") long id,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }
        String processedBy = "SECRETARIADO";
        if (session != null) {
            Object attr = session.getAttribute(AuthController.SESSION_USER_NAME);
            if (attr != null) {
                processedBy = attr.toString();
            }
        }
        boolean denied = preRegistrationService.deny(id, processedBy);
        if (denied) {
            redirectAttributes.addFlashAttribute("success", "Pré-inscrição negada e removida da fila.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Não foi possível negar esta pré-inscrição.");
        }
        return "redirect:/admin/users";
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
        model.addAttribute("preRegistrations", preRegistrationService.listPending());
        model.addAttribute("pendingPreCount", preRegistrationService.countPending());
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("secretariadofuncaoOptions", SECRETARIADO_FUNCOES);
        return "admin_user_list";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        if (!isSecretariat(session)) {
            redirectAttributes.addFlashAttribute("error", "Acesso restrito ao Secretariado.");
            return "redirect:/login";
        }

        if (session != null) {
            Object currentId = session.getAttribute(AuthController.SESSION_USER_ID);
            if (currentId != null) {
                long loggedId;
                if (currentId instanceof Number number) {
                    loggedId = number.longValue();
                } else {
                    try {
                        loggedId = Long.parseLong(currentId.toString());
                    } catch (NumberFormatException ex) {
                        loggedId = -1;
                    }
                }
                if (loggedId == id) {
                    redirectAttributes.addFlashAttribute("error", "Você não pode excluir sua própria conta enquanto estiver logado.");
                    return "redirect:/admin/users";
                }
            }
        }

        boolean deleted = userAccountService.deleteUser(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Usu\u00e1rio removido definitivamente da plataforma.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Usu\u00e1rio n\u00e3o encontrado ou j\u00e1 removido.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(value = "preId", required = false) Long preId,
                                 Model model,
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
        if (preId != null) {
            Optional<PreRegistration> preOpt = preRegistrationService.findById(preId);
            if (preOpt.isEmpty() || preOpt.get().getStatus() != PreRegistration.Status.PENDENTE) {
                redirectAttributes.addFlashAttribute("error", "Pré-inscrição não encontrada ou já processada.");
                return "redirect:/admin/users";
            }
            PreRegistration pre = preOpt.get();
            UserCreateForm form = (UserCreateForm) model.getAttribute("form");
            if (form == null) {
                form = new UserCreateForm();
            }
            form.setPreRegistrationId(pre.getId());
            if (!StringUtils.hasText(form.getName())) {
                form.setName(pre.getNome());
            }
            if (!StringUtils.hasText(form.getEmail())) {
                form.setEmail(pre.getEmail());
            }
            if (!StringUtils.hasText(form.getInstituicao())) {
                form.setInstituicao(pre.getInstituicao());
            }
            if (!StringUtils.hasText(form.getTelefone())) {
                form.setTelefone(pre.getTelefone());
            }
            if (!StringUtils.hasText(form.getcomitePreferido())) {
                form.setcomitePreferido(pre.getcomitePreferido());
            }
            if (!StringUtils.hasText(form.getObservacoes())) {
                form.setObservacoes(pre.getMensagem());
            }
            model.addAttribute("form", form);
            model.addAttribute("preRegistration", pre);
        }
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("secretariadofuncaoOptions", SECRETARIADO_FUNCOES);
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
        model.addAttribute("secretariadofuncaoOptions", SECRETARIADO_FUNCOES);

        String name = form.getName() == null ? "" : form.getName().trim();
        String email = userAccountService.normalizeEmail(form.getEmail());
        String Instituicao = trimToNull(form.getInstituicao());
        String telefone = trimToNull(form.getTelefone());
        String comitePreferido = trimToNull(form.getcomitePreferido());
        String observacoes = trimToNull(form.getObservacoes());

        if (name.isBlank()) {
            model.addAttribute("error", "Informe o nome completo do usuÇ­rio.");
            return "admin_user_create";
        }
        if (!userAccountService.isValidEmail(email)) {
            model.addAttribute("error", "Informe um e-mail vÇ­lido.");
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
            model.addAttribute("error", "Este e-mail jÇ­ estÇ­ cadastrado.");
            return "admin_user_create";
        }
        if (!StringUtils.hasText(Instituicao) || !StringUtils.hasText(telefone) || !StringUtils.hasText(comitePreferido)) {
            model.addAttribute("error", "Instituição, telefone e comitê/área de interesse são obrigatórios.");
            return "admin_user_create";
        }

        String tipo = form.getTipo() == null || form.getTipo().isBlank() ? "DELEGADO" : form.getTipo().trim();
        if ("SECRETARIADO".equalsIgnoreCase(tipo)) {
            if (form.getSecretariadofuncao() == null || form.getSecretariadofuncao().isBlank()
                    || form.getSecretariadoDepartamento() == null || form.getSecretariadoDepartamento().isBlank()
                    || form.getSecretariadoresponsabilidades() == null || form.getSecretariadoresponsabilidades().isBlank()) {
                model.addAttribute("error", "Informe funcao, departamento e responsabilidades para o Secretariado.");
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
        createdOpt.ifPresent(created -> {
            if ("SECRETARIADO".equalsIgnoreCase(tipo)) {
                userAccountService.upsertSecretariadoProfile(created.id(),
                        new UserAccountService.SecretariadoProfile(
                                form.getSecretariadofuncao().trim().toUpperCase(Locale.ROOT),
                                form.getSecretariadoDepartamento().trim(),
                                safe(form.getSecretariadoMatricula()),
                                safe(form.getSecretariadoTelefone()),
                                safe(form.getSecretariadoTurno()),
                                safe(form.getSecretariadoresponsabilidades())
                        ));
            }
            userAccountService.upsertUserProfileDetails(created.id(),
                    new UserAccountService.UserProfileDetails(
                            Instituicao,
                            telefone,
                            comitePreferido,
                            observacoes
                    ));
        });

        sendWelcomeEmail(name, email, tipo, form.getPassword());
        if (form.getPreRegistrationId() != null) {
            String processedBy = "SECRETARIADO";
            if (session != null) {
                Object attr = session.getAttribute(AuthController.SESSION_USER_NAME);
                if (attr != null) {
                    processedBy = attr.toString();
                }
            }
            preRegistrationService.markProcessed(form.getPreRegistrationId(), processedBy);
        }

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
            redirectAttributes.addFlashAttribute("error", "Usuário Nao encontrado.");
            return "redirect:/admin/users";
        }

        UserAccountService.UserRecord user = userOpt.get();
        UserEditForm form = new UserEditForm();
        form.setName(user.name());
        form.setEmail(user.email());
        form.setTipo(user.type());
        userAccountService.findUserProfileDetails(user.id()).ifPresent(details -> {
            form.setInstituicao(details.Instituicao());
            form.setTelefone(details.telefone());
            form.setcomitePreferido(details.comitePreferido());
            form.setObservacoes(details.observacoes());
        });

        if ("SECRETARIADO".equalsIgnoreCase(user.type())) {
            userAccountService.findSecretariadoProfile(user.id()).ifPresent(profile -> {
                form.setSecretariadofuncao(profile.funcao());
                form.setSecretariadoDepartamento(profile.departamento());
                form.setSecretariadoMatricula(profile.matricula());
                form.setSecretariadoTelefone(profile.telefone());
                form.setSecretariadoTurno(profile.turnoAtendimento());
                form.setSecretariadoresponsabilidades(profile.responsabilidades());
            });
        }

        model.addAttribute("user", user);
        model.addAttribute("form", form);
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        model.addAttribute("secretariadofuncaoOptions", SECRETARIADO_FUNCOES);
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
            redirectAttributes.addFlashAttribute("error", "Usuário Nao encontrado.");
            return "redirect:/admin/users";
        }

        UserAccountService.UserRecord existing = existingOpt.get();

        String name = form.getName() == null ? "" : form.getName().trim();
        String email = userAccountService.normalizeEmail(form.getEmail());
        if (name.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Informe o nome do Usuário.");
            return String.format("redirect:/admin/users/%d/edit", id);
        }
        if (!userAccountService.isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Informe um e-mail válido.");
            return String.format("redirect:/admin/users/%d/edit", id);
        }

        String tipo = form.getTipo() == null ? existing.type() : form.getTipo().trim().toUpperCase(Locale.ROOT);
        String Instituicao = trimToNull(form.getInstituicao());
        String telefone = trimToNull(form.getTelefone());
        String comitePreferido = trimToNull(form.getcomitePreferido());
        String observacoes = trimToNull(form.getObservacoes());
        if ("SECRETARIADO".equalsIgnoreCase(tipo)) {
            if (form.getSecretariadofuncao() == null || form.getSecretariadofuncao().isBlank()
                    || form.getSecretariadoDepartamento() == null || form.getSecretariadoDepartamento().isBlank()
                    || form.getSecretariadoresponsabilidades() == null || form.getSecretariadoresponsabilidades().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "Informe funcao, departamento e responsabilidades para o Secretariado.");
                return String.format("redirect:/admin/users/%d/edit", id);
            }
        }
        if (!StringUtils.hasText(Instituicao) || !StringUtils.hasText(telefone) || !StringUtils.hasText(comitePreferido)) {
            redirectAttributes.addFlashAttribute("error", "Instituição, telefone e comitê/área de interesse são obrigatórios.");
            return String.format("redirect:/admin/users/%d/edit", id);
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
                                form.getSecretariadofuncao().trim().toUpperCase(Locale.ROOT),
                                form.getSecretariadoDepartamento().trim(),
                                safe(form.getSecretariadoMatricula()),
                                safe(form.getSecretariadoTelefone()),
                                safe(form.getSecretariadoTurno()),
                                safe(form.getSecretariadoresponsabilidades())
                        ));
            } else {
                userAccountService.deleteSecretariadoProfile(updated.id());
            }
            userAccountService.upsertUserProfileDetails(updated.id(),
                    new UserAccountService.UserProfileDetails(
                            Instituicao,
                            telefone,
                            comitePreferido,
                            observacoes
                    ));
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
            redirectAttributes.addFlashAttribute("error", "Usuário Nao encontrado.");
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
        body.append("Ola ").append(safe(updated.name())).append(",\n\n");
        body.append("O Secretariado atualizou o seu cadastro no MOCS ON em ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append(".\n");

        boolean anyChange = false;
        if (!safe(previous.name()).equals(updated.name())) {
            body.append("- Nome: ").append(safe(previous.name())).append(" â†’ ").append(safe(updated.name())).append("\n");
            anyChange = true;
        }
        if (!previous.email().equalsIgnoreCase(updated.email())) {
            body.append("- E-mail: ").append(previous.email()).append(" â†’ ").append(updated.email()).append("\n");
            anyChange = true;
        }
        if (!safe(previous.type()).equalsIgnoreCase(safe(updated.type()))) {
            body.append("- Papel: ").append(safe(previous.type())).append(" â†’ ").append(safe(updated.type())).append("\n");
            anyChange = true;
        }
        if (newPasswordPlain != null) {
            body.append("- Senha redefinida. Nova Senha temporária: ").append(newPasswordPlain).append("\n");
            anyChange = true;
        }

        if (!anyChange) {
            return;
        }

        body.append("\nSe você não reconhece esta alteração, procure o Secretariado imediatamente.\n\n");
        body.append("Equipe MOCS ON\n");

        emailService.send(emailDestino, "MOCS ON - Atualizacao do seu cadastro", body.toString());
    }

    private void sendWelcomeEmail(String name, String email, String tipo, String plainPassword) {
        StringBuilder body = new StringBuilder();
        body.append("Ola ").append(safe(name)).append(",\n\n");
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
        Object rOla = session.getAttribute(AuthController.SESSION_USER_ROLE);
        return rOla != null && "SECRETARIADO".equalsIgnoreCase(role.toString());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
        private String Instituicao;
        private String telefone;
        private String comitePreferido;
        private String observacoes;
        private String password;
        private String confirmPassword;
        private String tipo;
        private String secretariadofuncao;
        private String secretariadoDepartamento;
        private String secretariadoMatricula;
        private String secretariadoTelefone;
        private String secretariadoTurno;
        private String secretariadoresponsabilidades;
        private Long preRegistrationId;

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

        public String getInstituicao() {
            return Instituicao;
        }

        public void setInstituicao(String Instituicao) {
            this.Instituicao = Instituicao;
        }

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getcomitePreferido() {
            return comitePreferido;
        }

        public void setcomitePreferido(String comitePreferido) {
            this.comitePreferido = comitePreferido;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
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

        public String getSecretariadofuncao() {
            return secretariadofuncao;
        }

        public void setSecretariadofuncao(String secretariadofuncao) {
            this.secretariadofuncao = secretariadofuncao;
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

        public String getSecretariadoresponsabilidades() {
            return secretariadoresponsabilidades;
        }

        public void setSecretariadoresponsabilidades(String secretariadoresponsabilidades) {
            this.secretariadoresponsabilidades = secretariadoresponsabilidades;
        }

        public Long getPreRegistrationId() {
            return preRegistrationId;
        }

        public void setPreRegistrationId(Long preRegistrationId) {
            this.preRegistrationId = preRegistrationId;
        }
    }

    public static class UserEditForm {
        private String name;
        private String email;
        private String tipo;
        private String Instituicao;
        private String telefone;
        private String comitePreferido;
        private String observacoes;
        private String newPassword;
        private String secretariadofuncao;
        private String secretariadoDepartamento;
        private String secretariadoMatricula;
        private String secretariadoTelefone;
        private String secretariadoTurno;
        private String secretariadoresponsabilidades;

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

        public String getInstituicao() {
            return Instituicao;
        }

        public void setInstituicao(String Instituicao) {
            this.Instituicao = Instituicao;
        }

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getcomitePreferido() {
            return comitePreferido;
        }

        public void setcomitePreferido(String comitePreferido) {
            this.comitePreferido = comitePreferido;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getSecretariadofuncao() {
            return secretariadofuncao;
        }

        public void setSecretariadofuncao(String secretariadofuncao) {
            this.secretariadofuncao = secretariadofuncao;
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

        public String getSecretariadoresponsabilidades() {
            return secretariadoresponsabilidades;
        }

        public void setSecretariadoresponsabilidades(String secretariadoresponsabilidades) {
            this.secretariadoresponsabilidades = secretariadoresponsabilidades;
        }
    }
}












