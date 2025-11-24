package com.mocs_on.controller;

import com.mocs_on.auth.RememberMeService;
import com.mocs_on.auth.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final RememberMeService rememberMeService;

    public AuthController(UserAccountService userAccountService,
                          BCryptPasswordEncoder passwordEncoder,
                          RememberMeService rememberMeService) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
        this.rememberMeService = rememberMeService;
    }

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
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

        rememberMeService.establishSession(user, request);
        rememberMeService.storeRememberMeCookie(response, normalizedEmail);
        return "redirect:/dashboard.html";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session, HttpServletResponse response) {
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        rememberMeService.clearRememberMeCookie(response);
        return "redirect:/login";
    }

    private String failLogin(String email, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "E-mail ou senha invalidos.");
        redirectAttributes.addFlashAttribute("email", email == null ? "" : email);
        return "redirect:/login";
    }

}
