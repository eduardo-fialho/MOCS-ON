package com.mocs_on.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String defaultFrom;

    public DeliveryStatus send(String to, String subject, String body) {
        if (mailSender == null) {
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body:\n{}", to, subject, body);
            return DeliveryStatus.FALLBACK;
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            if (defaultFrom != null && !defaultFrom.isBlank()) {
                helper.setFrom(defaultFrom);
            }
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            return DeliveryStatus.SENT;
        } catch (MailException ex) {
            log.warn("Falha ao enviar e-mail. Caindo para o fallback. erro={}", ex.getMessage());
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body:\n{}", to, subject, body);
            return DeliveryStatus.FALLBACK;
        } catch (Exception ex) {
            log.warn("Falha inesperada ao montar e-mail. Caindo para o fallback. erro={}", ex.getMessage());
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body:\n{}", to, subject, body);
            return DeliveryStatus.FALLBACK;
        }
    }

    public enum DeliveryStatus {
        SENT,
        FALLBACK
    }
}
