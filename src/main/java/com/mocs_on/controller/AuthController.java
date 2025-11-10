package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.SecaoUsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    public static final String SESSION_USER_ATTRIBUTE = "AUTH_USER_EMAIL";
    public static final String SESSION_USER_NAME = "AUTH_USER_NAME";
    public static final String SESSION_USER_ROLE = "AUTH_USER_ROLE";
    public static final String SESSION_USER_ID = "AUTH_USER_ID";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecaoUsuarioService secaoUsuarioService;

    public AuthController(UserAccountService userAccountService,
                          BCryptPasswordEncoder passwordEncoder,
                          SecaoUsuarioService secaoUsuarioService) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
        this.secaoUsuarioService = secaoUsuarioService;
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {
        String normalizedEmail = userAccountService.normalizeEmail(email);
        if (!userAccountService.isValidEmail(normalizedEmail)) {
            return failLogin(email, redirectAttributes);
        }

        Optional<UserAccountService.UserRecord> userOpt = userAccountService.findUserByEmail(normalizedEmail);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().passwordHash())) {
            return failLogin(email, redirectAttributes);
        }
        UserAccountService.UserRecord user = userOpt.get();

        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER_ATTRIBUTE, normalizedEmail);
        session.setAttribute(SESSION_USER_NAME, user.name());
        session.setAttribute(SESSION_USER_ROLE, user.type());
        session.setAttribute(SESSION_USER_ID, user.id());
        registerSecurityContext(session, normalizedEmail);
        return "redirect:/dashboard.html";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    private String failLogin(String email, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "E-mail ou senha invalidos.");
        redirectAttributes.addFlashAttribute("email", email == null ? "" : email);
        return "redirect:/login";
    }

    private void registerSecurityContext(HttpSession session, String email) {
        if (session == null) {
            return;
        }
        try {
            SecaoUsuario userDetails = (SecaoUsuario) secaoUsuarioService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            userDetails.getPassword(),
                            userDetails.getAuthorities()
                    );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        } catch (UsernameNotFoundException ex) {
            LOGGER.warn("Nao foi possivel registrar SecurityContext para {}: {}", email, ex.getMessage());
        } catch (Exception ex) {
            LOGGER.warn("Falha inesperada ao registrar SecurityContext para {}: {}", email, ex.getMessage());
        }
    }
}
