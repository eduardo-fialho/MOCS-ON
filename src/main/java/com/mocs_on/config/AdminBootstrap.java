package com.mocs_on.config;

import com.mocs_on.auth.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria automaticamente um usuario do Secretariado na primeira execucao
 * para evitar dependencias de scripts manuais.
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap.admin-email:admin@mocson.local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password:Admin@123}")
    private String adminPassword;

    @Value("${app.bootstrap.admin-name:Secretariado MOCS}")
    private String adminName;

    public AdminBootstrap(UserAccountService userAccountService,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        userAccountService.ensureCoreTables();
        String normalized = userAccountService.normalizeEmail(adminEmail);
        if (!userAccountService.isValidEmail(normalized)) {
            log.warn("Bootstrap desativado: e-mail invalido configurado ({}).", adminEmail);
            return;
        }
        if (userAccountService.userExists(normalized)) {
            return;
        }
        String hash = passwordEncoder.encode(adminPassword);
        userAccountService.createUser(adminName, normalized, hash, "SECRETARIADO");
        log.info("Usuario administrativo criado automaticamente: {}", normalized);
    }
}
