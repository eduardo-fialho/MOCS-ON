package com.mocs_on.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void send(String to, String subject, String body) {
        if (mailSender == null) {
            // Fallback de desenvolvimento
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body: {}", to, subject, body);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        try {
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail, usando fallback. erro={}", e.toString());
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body: {}", to, subject, body);
        }
    }
}

