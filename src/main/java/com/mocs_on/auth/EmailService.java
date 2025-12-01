package com.mocs_on.auth;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final Path DEFAULT_CONFIG_PATH = Paths.get("config", "smtp_config.properties");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String defaultFrom;

    public DeliveryStatus send(String to, String subject, String body) {
        JavaMailSender sender = this.mailSender;
        SmtpSettings settings = loadSettings();

        if (sender == null) {
            if (settings == null || !settings.isValid()) {
                log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body:\n{}", to, subject, body);
                log.info("Dica: configure SMTP_* como variáveis de ambiente ou edite config/smtp_config.properties.");
                return DeliveryStatus.FALLBACK;
            }
            sender = buildSenderFromSettings(settings);
        } else if ((defaultFrom == null || defaultFrom.isBlank()) && settings != null && settings.fromEmail != null) {
            defaultFrom = settings.fromEmail;
        }

        String fromEmail = resolveFromEmail(settings);
        String fromName = settings != null && settings.fromName != null ? settings.fromName : "";

        try {
            var message = sender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            if (fromEmail != null && !fromEmail.isBlank()) {
                if (fromName != null && !fromName.isBlank()) {
                    helper.setFrom(fromEmail, fromName);
                } else {
                    helper.setFrom(fromEmail);
                }
            }
            helper.setSubject(subject);
            helper.setText(body, false);
            sender.send(message);
            return DeliveryStatus.SENT;
        } catch (MailException | MessagingException ex) {
            log.warn("Falha ao enviar e-mail. Caindo para o fallback. erro={}", ex.getMessage());
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body:\n{}", to, subject, body);
            return DeliveryStatus.FALLBACK;
        } catch (Exception ex) {
            log.warn("Falha inesperada ao montar e-mail. Caindo para o fallback. erro={}", ex.getMessage());
            log.info("RESET_LINK mail fallback -> to: {} | subject: {} | body:\n{}", to, subject, body);
            return DeliveryStatus.FALLBACK;
        }
    }

    private String resolveFromEmail(SmtpSettings settings) {
        if (defaultFrom != null && !defaultFrom.isBlank()) {
            return defaultFrom;
        }
        if (settings != null && settings.fromEmail != null && !settings.fromEmail.isBlank()) {
            return settings.fromEmail;
        }
        if (settings != null && settings.username != null && !settings.username.isBlank()) {
            return settings.username;
        }
        return null;
    }

    private JavaMailSender buildSenderFromSettings(SmtpSettings settings) {
        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        impl.setHost(settings.host);
        impl.setPort(settings.port);
        impl.setUsername(settings.username);
        impl.setPassword(settings.password);
        impl.setProtocol(settings.useSsl ? "smtps" : "smtp");

        Properties props = impl.getJavaMailProperties();
        props.put("mail.smtp.auth", Boolean.toString(settings.useAuth));
        props.put("mail.smtp.starttls.enable", Boolean.toString(settings.useStartTls));
        props.put("mail.smtp.ssl.enable", Boolean.toString(settings.useSsl));
        props.put("mail.smtp.connectiontimeout", Integer.toString(settings.timeoutMs));
        props.put("mail.smtp.timeout", Integer.toString(settings.timeoutMs));
        props.put("mail.smtp.writetimeout", Integer.toString(settings.timeoutMs));
        return impl;
    }

    private SmtpSettings loadSettings() {
        SmtpSettings env = SmtpSettings.fromEnvironment();
        if (env != null && env.isValid()) {
            return env;
        }
        SmtpSettings file = SmtpSettings.fromPropertiesFile(DEFAULT_CONFIG_PATH);
        if (file != null && file.isValid()) {
            return file;
        }
        return null;
    }

    public enum DeliveryStatus {
        SENT,
        FALLBACK
    }

    private static class SmtpSettings {
        String host;
        int port = 587;
        String username;
        String password;
        String fromEmail;
        String fromName;
        boolean useStartTls = true;
        boolean useSsl = false;
        boolean useAuth = true;
        int timeoutMs = 5000;

        boolean isValid() {
            return host != null && !host.isBlank() && fromEmail() != null;
        }

        private String fromEmail() {
            if (fromEmail != null && !fromEmail.isBlank()) {
                return fromEmail;
            }
            if (username != null && !username.isBlank()) {
                return username;
            }
            return null;
        }

        static SmtpSettings fromEnvironment() {
            String host = System.getenv("SMTP_HOST");
            if (host == null || host.isBlank()) {
                return null;
            }
            SmtpSettings settings = new SmtpSettings();
            settings.host = host;
            settings.port = parseInt(System.getenv("SMTP_PORT"), 587);
            settings.username = System.getenv("SMTP_USERNAME");
            settings.password = System.getenv("SMTP_PASSWORD");
            settings.fromEmail = System.getenv("SMTP_FROM_EMAIL");
            settings.fromName = System.getenv("SMTP_FROM_NAME");
            settings.useStartTls = parseBoolean(System.getenv("SMTP_USE_STARTTLS"), true);
            settings.useSsl = parseBoolean(System.getenv("SMTP_USE_SSL"), false);
            settings.useAuth = parseBoolean(System.getenv("SMTP_USE_AUTH"), settings.username != null && !settings.username.isBlank());
            settings.timeoutMs = parseInt(System.getenv("SMTP_TIMEOUT_MS"), 5000);
            return settings;
        }

        static SmtpSettings fromPropertiesFile(Path path) {
            if (!Files.exists(path)) {
                return null;
            }
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException ex) {
                log.warn("Não foi possível ler {}: {}", path, ex.getMessage());
                return null;
            }
            SmtpSettings settings = new SmtpSettings();
            settings.host = props.getProperty("host");
            settings.port = parseInt(props.getProperty("port"), 587);
            settings.username = props.getProperty("username");
            settings.password = props.getProperty("password");
            settings.fromEmail = props.getProperty("from_email");
            settings.fromName = props.getProperty("from_name");
            settings.useStartTls = parseBoolean(props.getProperty("use_starttls"), true);
            settings.useSsl = parseBoolean(props.getProperty("use_ssl"), false);
            settings.useAuth = parseBoolean(props.getProperty("use_auth"), settings.username != null && !settings.username.isBlank());
            settings.timeoutMs = parseInt(props.getProperty("timeout_ms"), 5000);
            return settings;
        }

        private static boolean parseBoolean(String value, boolean defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            return value.equalsIgnoreCase("true")
                    || value.equalsIgnoreCase("1")
                    || value.equalsIgnoreCase("yes")
                    || value.equalsIgnoreCase("on");
        }

        private static int parseInt(String value, int defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
    }
}
