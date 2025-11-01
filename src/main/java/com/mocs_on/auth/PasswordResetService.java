package com.mocs_on.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.users.table:usuarios}")
    private String usersTable;

    @Value("${app.users.email-column:email}")
    private String usersEmailColumn;

    @Value("${app.users.password-column:senha}")
    private String usersPasswordColumn;

    @Value("${app.reset.expiration-minutes:60}")
    private long expirationMinutes;

    @Value("${app.base-url:http://localhost:8082}")
    private String appBaseUrl;

    public PasswordResetService(JdbcTemplate jdbcTemplate, EmailService emailService) {
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
    }

    public void requestReset(String emailRaw, String ip, String userAgent) {
        String email = normalizeEmail(emailRaw);
        String token = generateToken();
        String tokenHash = sha256Hex(token);
        Instant expires = Instant.now().plus(Duration.ofMinutes(expirationMinutes));

        jdbcTemplate.update(
                "INSERT INTO password_reset_tokens (email, token_hash, expires_at, created_at, ip, user_agent) VALUES (?,?,?,?,?,?)",
                email,
                tokenHash,
                Timestamp.from(expires),
                new Timestamp(System.currentTimeMillis()),
                ip,
                userAgent
        );

        String link = appBaseUrl.replaceAll("/$", "") + "/auth/reset-password?token=" + token;
        String subject = "MOCS ON - Vamos redefinir sua senha";
        String body = String.format(
                "Ola!%n%nRecebemos o seu pedido para redefinir a senha do portal MOCS ON.%n%n" +
                        "Para continuar, clique no link abaixo (valido por %d minutos):%n%s%n%n" +
                        "Se nao foi voce, basta ignorar esta mensagem.%n%n" +
                        "Conte com a gente — estamos fazendo de tudo para tornar a sua experiencia com o MOCS a melhor possivel.%n",
                expirationMinutes,
                link
        );

        emailService.send(email, subject, body);
    }

    public boolean resetPassword(String tokenRaw, String newPassword) {
        String token = tokenRaw == null ? "" : tokenRaw.trim();
        String tokenHash = sha256Hex(token);

        Optional<TokenRow> rowOpt = jdbcTemplate.query(
                "SELECT id, email, expires_at, used_at FROM password_reset_tokens WHERE token_hash = ?",
                rs -> {
                    if (rs.next()) {
                        TokenRow row = new TokenRow();
                        row.id = rs.getLong("id");
                        row.email = rs.getString("email");
                        row.expiresAt = rs.getTimestamp("expires_at");
                        row.usedAt = rs.getTimestamp("used_at");
                        return Optional.of(row);
                    }
                    return Optional.empty();
                },
                tokenHash
        );

        if (rowOpt.isEmpty()) {
            return false;
        }

        TokenRow row = rowOpt.get();
        if (row.usedAt != null) {
            return false;
        }
        if (row.expiresAt != null && row.expiresAt.toInstant().isBefore(Instant.now())) {
            return false;
        }

        String email = normalizeEmail(row.email);
        String hash = encoder.encode(newPassword);

        int updated = jdbcTemplate.update(
                String.format("UPDATE `%s` SET `%s` = ? WHERE LOWER(`%s`) = LOWER(?)",
                        usersTable,
                        usersPasswordColumn,
                        usersEmailColumn),
                hash,
                email
        );

        if (updated == 0) {
            return false;
        }

        jdbcTemplate.update("UPDATE password_reset_tokens SET used_at = NOW() WHERE id = ?", row.id);
        return true;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String generateToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        StringBuilder sb = new StringBuilder(buf.length * 2);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class TokenRow {
        long id;
        String email;
        Timestamp expiresAt;
        Timestamp usedAt;
    }
}
