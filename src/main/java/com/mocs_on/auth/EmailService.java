package com.mocs_on.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String defaultFrom;

    public void send(String to, String subject, String body) {
        if (mailSender == null) {
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body=omitted", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        if (defaultFrom != null && !defaultFrom.isBlank()) {
            message.setFrom(defaultFrom);
        }
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Falha ao enviar e-mail. Caindo para o fallback. erro={}", ex.getMessage());
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body=omitted", to, subject);
        }
    }
}
