package com.mocs_on.service;

import com.mocs_on.auth.EmailService;
import com.mocs_on.auth.UserAccountService;
import com.mocs_on.domain.PreRegistration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PreRegistrationService {

    private final PreRegistrationDAO preRegistrationDAO;
    private final EmailService emailService;
    private final UserAccountService userAccountService;

    @Value("${app.secretariat.email:}")
    private String secretariatEmail;

    public PreRegistrationService(PreRegistrationDAO preRegistrationDAO, EmailService emailService, UserAccountService userAccountService) {
        this.preRegistrationDAO = preRegistrationDAO;
        this.emailService = emailService;
        this.userAccountService = userAccountService;
    }

    public long registerInterest(PreRegistration registration) {
        long id = preRegistrationDAO.insert(registration);
        notifySecretariat(registration);
        return id;
    }

    public List<PreRegistration> listPending() {
        return preRegistrationDAO.findPending();
    }

    public int countPending() {
        return preRegistrationDAO.countPending();
    }

    public Optional<PreRegistration> findById(long id) {
        return preRegistrationDAO.findById(id);
    }

    public void markProcessed(long id, String processedBy) {
        preRegistrationDAO.markProcessed(id, processedBy);
    }

    public boolean deny(long id, String processedBy) {
        return preRegistrationDAO.markDenied(id, processedBy);
    }

    public void delete(long id) {
        preRegistrationDAO.delete(id);
    }

    private void notifySecretariat(PreRegistration registration) {
        java.util.LinkedHashSet<String> recipients = new java.util.LinkedHashSet<>(userAccountService.findEmailsByRole("SECRETARIADO"));
        if (StringUtils.hasText(secretariatEmail)) {
            recipients.add(secretariatEmail.trim());
        }
        String subject = "Nova pre-inscricao recebida";
        String body = """
                Uma nova pre-inscricao foi enviada pelo portal:

                Nome: %s
                E-mail: %s
                Instituicao: %s
                Telefone: %s
                Comite preferido: %s

                Mensagem:
                %s

                Acesse o painel de Gestao de Usuarios para dar sequencia no cadastro.
                """.formatted(
                nullSafe(registration.getNome()),
                nullSafe(registration.getEmail()),
                nullSafe(registration.getInstituicao()),
                nullSafe(registration.getTelefone()),
                nullSafe(registration.getComitePreferido()),
                nullSafe(registration.getMensagem())
        );
        for (String to : recipients) {
            if (StringUtils.hasText(to)) {
                emailService.send(to, subject, body);
            }
        }
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}










