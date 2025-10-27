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

    private final JdbcTemplate jdbc;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.users.table:users}")
    private String usersTable;

    @Value("${app.users.email-column:email}")
    private String usersEmailCol;

    @Value("${app.users.password-column:password_hash}")
    private String usersPasswordCol;

    @Value("${app.reset.expiration-minutes:60}")
    private long expirationMinutes;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    public PasswordResetService(JdbcTemplate jdbc, EmailService emailService) {
        this.jdbc = jdbc;
        this.emailService = emailService;
    }

    public void requestReset(String emailRaw, String ip, String userAgent) {
        String email = normalizeEmail(emailRaw);
        String token = generateToken();
        String tokenHash = sha256Hex(token);

        Instant expires = Instant.now().plus(Duration.ofMinutes(expirationMinutes));

        jdbc.update(
                "INSERT INTO password_reset_tokens (email, token_hash, expires_at, created_at, ip, user_agent) VALUES (?,?,?,?,?,?)",
                email, tokenHash, Timestamp.from(expires), new Timestamp(System.currentTimeMillis()), ip, userAgent
        );

        String link = appBaseUrl.replaceAll("/$", "") + "/auth/reset-password?token=" + token;
        String subject = "Instruções para redefinição de senha";
        String body = "Olá,\n\nRecebemos uma solicitação para redefinir sua senha.\n" +
                "Acesse o link abaixo (válido por " + expirationMinutes + " min):\n" + link + "\n\nSe você não solicitou, ignore este e-mail.";

        emailService.send(email, subject, body);
    }

    public boolean resetPassword(String tokenRaw, String newPassword) {
        String token = tokenRaw == null ? "" : tokenRaw.trim();
        String tokenHash = sha256Hex(token);

        Optional<ResetRow> rowOpt = jdbc.query("SELECT id, email, expires_at, used_at FROM password_reset_tokens WHERE token_hash = ?",
                rs -> {
                    if (rs.next()) {
                        ResetRow r = new ResetRow();
                        r.id = rs.getLong("id");
                        r.email = rs.getString("email");
                        r.expiresAt = rs.getTimestamp("expires_at");
                        r.usedAt = rs.getTimestamp("used_at");
                        return Optional.of(r);
                    }
                    return Optional.empty();
                }, tokenHash);

        if (rowOpt.isEmpty()) return false;
        ResetRow row = rowOpt.get();
        if (row.usedAt != null) return false;
        if (row.expiresAt != null && row.expiresAt.toInstant().isBefore(Instant.now())) return false;

        String email = normalizeEmail(row.email);
        String hash = encoder.encode(newPassword);

        // Atualiza senha (case-insensitive match)
        int updated = jdbc.update(
                String.format("UPDATE `%s` SET `%s` = ? WHERE LOWER(`%s`) = LOWER(?)", usersTable, usersPasswordCol, usersEmailCol),
                hash, email
        );
        if (updated == 0) return false;

        jdbc.update("UPDATE password_reset_tokens SET used_at = NOW() WHERE id = ?", row.id);
        return true;
    }

    static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    static String generateToken() {
        byte[] buf = new byte[32];
        new SecureRandom().nextBytes(buf);
        StringBuilder sb = new StringBuilder(buf.length * 2);
        for (byte b : buf) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class ResetRow {
        long id;
        String email;
        Timestamp expiresAt;
        Timestamp usedAt;
    }
}

