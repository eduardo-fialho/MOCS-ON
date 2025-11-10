package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final UserAccountService userAccountService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserAccountService userAccountService, BCryptPasswordEncoder passwordEncoder) {
        this.userAccountService = userAccountService;
        this.passwordEncoder = passwordEncoder;
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
        return "redirect:/dashboard.html";
    }

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    private String failLogin(String email, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "E-mail ou senha invalidos.");
        redirectAttributes.addFlashAttribute("email", email == null ? "" : email);
        return "redirect:/login";
    }
}
