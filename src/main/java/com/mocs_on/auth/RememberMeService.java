package com.mocs_on.auth;

import com.mocs_on.controller.AuthController;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.SecaoUsuarioService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Component
public class RememberMeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RememberMeService.class);
    private static final String COOKIE_NAME = "MOCSON_REMEMBER";
    private static final Duration COOKIE_TTL = Duration.ofDays(7);

    private final UserAccountService userAccountService;
    private final SecaoUsuarioService secaoUsuarioService;
    private final SecretKeySpec secretKey;

    public RememberMeService(UserAccountService userAccountService,
                             SecaoUsuarioService secaoUsuarioService,
                             @Value("${app.remember.secret:MOCS_ON_REMEMBER_SECRET}") String secret) {
        this.userAccountService = userAccountService;
        this.secaoUsuarioService = secaoUsuarioService;
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public void establishSession(UserAccountService.UserRecord user, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthController.SESSION_USER_ATTRIBUTE, user.email());
        session.setAttribute(AuthController.SESSION_USER_NAME, user.name());
        session.setAttribute(AuthController.SESSION_USER_ROLE, user.type());
        session.setAttribute(AuthController.SESSION_USER_ID, user.id());
        registerSecurityContext(session, user.email());
    }

    public void storeRememberMeCookie(HttpServletResponse response, String email) {
        String normalized = userAccountService.normalizeEmail(email);
        long expiresAt = System.currentTimeMillis() + COOKIE_TTL.toMillis();
        String payload = normalized + ":" + expiresAt;
        String signature = sign(payload);
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + ":" + signature).getBytes(StandardCharsets.UTF_8));

        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) COOKIE_TTL.getSeconds());
        response.addCookie(cookie);
    }

    public void clearRememberMeCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public boolean tryRestoreSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession current = request.getSession(false);
        if (current != null && current.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null) {
            return true;
        }

        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie == null || cookie.getValue().isBlank()) {
            return false;
        }

        Optional<UserAccountService.UserRecord> userOpt = decodeToken(cookie.getValue());
        if (userOpt.isEmpty()) {
            if (response != null) {
                clearRememberMeCookie(response);
            }
            return false;
        }

        establishSession(userOpt.get(), request);
        if (response != null) {
            storeRememberMeCookie(response, userOpt.get().email());
        }
        return true;
    }

    private Optional<UserAccountService.UserRecord> decodeToken(String tokenValue) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(tokenValue), StandardCharsets.UTF_8);
            String[] parts = raw.split(":");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String email = parts[0];
            long expires = Long.parseLong(parts[1]);
            String providedSignature = parts[2];

            if (expires < System.currentTimeMillis()) {
                return Optional.empty();
            }

            String expectedSignature = sign(email + ":" + expires);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            String normalized = userAccountService.normalizeEmail(email);
            return userAccountService.findUserByEmail(normalized);
        } catch (Exception ex) {
            LOGGER.debug("Falha ao decodificar remember-me: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] result = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível assinar o token remember-me", ex);
        }
    }

    private void registerSecurityContext(HttpSession session, String email) {
        if (session == null) {
            return;
        }
        try {
            SecaoUsuario userDetails = (SecaoUsuario) secaoUsuarioService.loadUserByUsername(email);
            var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities()
            );
            var context = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            org.springframework.security.core.context.SecurityContextHolder.setContext(context);
            session.setAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        } catch (Exception ex) {
            LOGGER.warn("Falha ao registrar SecurityContext: {}", ex.getMessage());
        }
    }
}
