package com.mocs_on.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final JdbcTemplate jdbcTemplate;
    private final EmailService emailService;
    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.reset.expiration-minutes:60}")
    private long expirationMinutes;

    @Value("${app.base-url:http://localhost:8082}")
    private String appBaseUrl;

    public PasswordResetService(JdbcTemplate jdbcTemplate,
                                EmailService emailService,
                                UserAccountService userAccountService,
                                BCryptPasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.emailService = emailService;
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    public RequestResetResult requestReset(String emailRaw, String ip, String userAgent) {
        String email = userAccountService.normalizeEmail(emailRaw);
        log.info("Password reset solicitado para {}", email != null ? email : emailRaw);
        if (!userAccountService.isValidEmail(email)) {
            log.info("Reset abortado: email inválido {}", emailRaw);
            return RequestResetResult.invalid();
        }
        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("Reset abortado: usuario não encontrado [{}]. Usuários cadastrados: {}",
                    email,
                    userAccountService.findAllUsers()
                            .stream()
                            .map(UserAccountService.UserRecord::email)
                            .toList());
            return RequestResetResult.invalid();
        }

        String token = generateToken();
        String tokenHash = sha256Hex(token);

        Instant now = Instant.now();
        Instant expires = now.plus(Duration.ofMinutes(expirationMinutes));

        jdbcTemplate.update(
                "INSERT INTO password_reset_tokens (email, token_hash, expires_at, created_at, ip, user_agent) VALUES (?,?,?,?,?,?)",
                email,
                tokenHash,
                Timestamp.from(expires),
                Timestamp.from(now),
                ip,
                userAgent
        );

        String link = sanitizeBaseUrl(appBaseUrl) + "/auth/reset-password?token=" + token;
        String subject = "MOCS ON - redefinicao de senha";
        String body = String.format(
                "Ola,%n%nRecebemos um pedido para redefinir a senha do portal MOCS ON.%n%n" +
                        "Use o link abaixo (valido por %d minutos):%n%s%n%n" +
                        "Se voce nao solicitou, ignore este e-mail.%n%n" +
                        "Equipe MOCS ON%n",
                expirationMinutes,
                link
        );

        EmailService.DeliveryStatus status = emailService.send(email, subject, body);
        log.info("Email de reset enviado para {}? {}", email, status);
        return RequestResetResult.of(status == EmailService.DeliveryStatus.SENT, link);
    }

    @Transactional
    public ResetPasswordStatus resetPassword(String tokenRaw, String newPassword) {
        String token = tokenRaw == null ? "" : tokenRaw.trim();
        if (token.isEmpty()) {
            return ResetPasswordStatus.INVALID_TOKEN;
        }

        String tokenHash = sha256Hex(token);

        List<TokenRow> rows = jdbcTemplate.query(
                "SELECT id, email, expires_at, used_at FROM password_reset_tokens WHERE token_hash = ?",
                (rs, rowNum) -> {
                    TokenRow row = new TokenRow();
                    row.id = rs.getLong("id");
                    row.email = rs.getString("email");
                    row.expiresAt = rs.getTimestamp("expires_at");
                    row.usedAt = rs.getTimestamp("used_at");
                    return row;
                },
                tokenHash
        );

        Optional<TokenRow> rowOpt = rows.stream().findFirst();
        if (rowOpt.isEmpty()) {
            return ResetPasswordStatus.INVALID_TOKEN;
        }

        TokenRow row = rowOpt.get();
        if (row.usedAt != null) {
            return ResetPasswordStatus.TOKEN_ALREADY_USED;
        }
        if (row.expiresAt != null && row.expiresAt.toInstant().isBefore(Instant.now())) {
            return ResetPasswordStatus.TOKEN_EXPIRED;
        }

        String email = userAccountService.normalizeEmail(row.email);
        if (!userAccountService.isValidEmail(email)) {
            return ResetPasswordStatus.INVALID_EMAIL;
        }

        Optional<String> currentHashOpt = userAccountService.findPasswordHashByEmail(email);
        if (currentHashOpt.isEmpty()) {
            return ResetPasswordStatus.INVALID_EMAIL;
        }

        if (passwordEncoder.matches(newPassword, currentHashOpt.get())) {
            return ResetPasswordStatus.SAME_PASSWORD;
        }

        String hash = passwordEncoder.encode(newPassword);
        int updated = userAccountService.updatePassword(email, hash);
        if (updated == 0) {
            return ResetPasswordStatus.UPDATE_FAILED;
        }

        jdbcTemplate.update(
                "UPDATE password_reset_tokens SET used_at = ? WHERE id = ?",
                Timestamp.from(Instant.now()),
                row.id
        );
        return ResetPasswordStatus.SUCCESS;
    }

    private static String sanitizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "";
        }
        return baseUrl.replaceAll("/$", "");
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

    public enum ResetPasswordStatus {
        SUCCESS,
        INVALID_TOKEN,
        TOKEN_ALREADY_USED,
        TOKEN_EXPIRED,
        INVALID_EMAIL,
        SAME_PASSWORD,
        UPDATE_FAILED
    }

    public record RequestResetResult(boolean delivered, String resetLink) {
        static RequestResetResult of(boolean delivered, String link) {
            return new RequestResetResult(delivered, link);
        }

        static RequestResetResult invalid() {
            return new RequestResetResult(false, null);
        }
    }
}
