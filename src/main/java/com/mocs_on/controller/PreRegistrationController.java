package com.mocs_on.controller;

import com.mocs_on.domain.PreRegistration;
import com.mocs_on.service.PreRegistrationService;
import com.mocs_on.service.UsuarioComiteDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class PreRegistrationController {

    private final PreRegistrationService preRegistrationService;
    private final UsuarioComiteDao usuarioComiteDao;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public PreRegistrationController(PreRegistrationService preRegistrationService,
                                     UsuarioComiteDao usuarioComiteDao) {
        this.preRegistrationService = preRegistrationService;
        this.usuarioComiteDao = usuarioComiteDao;
    }

    @GetMapping("/preinscricao")
    public String showForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PreRegistrationForm());
        }
        model.addAttribute("comites", usuarioComiteDao.listarComites());
        return "pre_registration_form";
    }

    @PostMapping("/preinscricao")
    public String submit(@ModelAttribute("form") PreRegistrationForm form,
                         RedirectAttributes redirectAttributes) {
        sanitizeForm(form);
        List<String> missing = validateRequiredFields(form);
        if (!missing.isEmpty()) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error",
                    "Preencha todos os campos obrigatórios: " + String.join(", ", missing) + ".");
            return "redirect:/preinscricao";
        }

        if (!isValidEmail(form.getEmail())) {
            redirectAttributes.addFlashAttribute("form", form);
            redirectAttributes.addFlashAttribute("error", "Informe um e-mail válido.");
            return "redirect:/preinscricao";
        }

        PreRegistration entity = new PreRegistration();
        entity.setNome(form.getNome());
        entity.setEmail(form.getEmail());
        entity.setInstituicao(form.getInstituicao());
        entity.setTelefone(form.getTelefone());
        entity.setComitePreferido(form.getComitePreferido());
        entity.setMensagem(form.getMensagem());

        preRegistrationService.registerInterest(entity);

        redirectAttributes.addFlashAttribute("success",
                "Recebemos sua pré-inscrição! O Secretariado entrará em contato em breve.");
        return "redirect:/preinscricao";
    }

    private void sanitizeForm(PreRegistrationForm form) {
        form.setNome(safeTrim(form.getNome()));
        form.setEmail(safeTrim(form.getEmail()));
        form.setInstituicao(safeTrim(form.getInstituicao()));
        form.setTelefone(safeTrim(form.getTelefone()));
        form.setComitePreferido(safeTrim(form.getComitePreferido()));
        form.setMensagem(safeTrim(form.getMensagem()));
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }

    private List<String> validateRequiredFields(PreRegistrationForm form) {
        List<String> missing = new ArrayList<>();
        if (!StringUtils.hasText(form.getNome())) {
            missing.add("Nome completo");
        }
        if (!StringUtils.hasText(form.getEmail())) {
            missing.add("E-mail");
        }
        if (!StringUtils.hasText(form.getInstituicao())) {
            missing.add("Instituição ou escola");
        }
        if (!StringUtils.hasText(form.getTelefone())) {
            missing.add("Telefone/WhatsApp");
        }
        if (!StringUtils.hasText(form.getComitePreferido())) {
            missing.add("Comitê ou área de interesse");
        }
        if (!StringUtils.hasText(form.getMensagem())) {
            missing.add("Mensagem adicional");
        }
        return missing;
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static class PreRegistrationForm {
        private String nome;
        private String email;
        private String instituicao;
        private String telefone;
        private String comitePreferido;
        private String mensagem;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getInstituicao() {
            return instituicao;
        }

        public void setInstituicao(String instituicao) {
            this.instituicao = instituicao;
        }

        public String getTelefone() {
            return telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getComitePreferido() {
            return comitePreferido;
        }

        public void setComitePreferido(String comitePreferido) {
            this.comitePreferido = comitePreferido;
        }

        public String getMensagem() {
            return mensagem;
        }

        public void setMensagem(String mensagem) {
            this.mensagem = mensagem;
        }
    }
}
