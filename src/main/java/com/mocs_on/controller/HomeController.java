package com.mocs_on.controller;

import com.mocs_on.auth.RememberMeService;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.PreRegistrationService;
import com.mocs_on.service.SecretariatDashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@CrossOrigin(origins = "*")
public class HomeController {

    private final RememberMeService rememberMeService;
    private final PreRegistrationService preRegistrationService;
    private final SecretariatDashboardService secretariatDashboardService;

    public HomeController(RememberMeService rememberMeService,
                          PreRegistrationService preRegistrationService,
                          SecretariatDashboardService secretariatDashboardService) {
        this.rememberMeService = rememberMeService;
        this.preRegistrationService = preRegistrationService;
        this.secretariatDashboardService = secretariatDashboardService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        if (rememberMeService.tryRestoreSession(request, response) ||
                (session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null)) {
            return "redirect:/dashboard.html";
        }
        if (!model.containsAttribute("email")) {
            model.addAttribute("email", "");
        }
        return "login";
    }

    @GetMapping("/login.html")
    public String legacyLoginPath() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard.html")
    public String dashboard(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        if (!ensureAuthenticated(request, response, session)) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "dashboard";
    }

    @GetMapping("/mesa_diretora.html")
    public String mesaDiretora(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        if (!ensureAuthenticated(request, response, session)) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "mesa_diretora";
    }

    @GetMapping("/secretariado.html")
    public String secretariado(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) {
        if (!ensureAuthenticated(request, response, session)) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        model.addAttribute("pendingPreCount", preRegistrationService.countPending());
        model.addAttribute("pendingPreRegistrations", preRegistrationService.listPending());
        model.addAttribute("dashboardMetrics", secretariatDashboardService.collectMetrics());
        return "secretariado";
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null;
    }

    private boolean isAuthenticatedSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof SecaoUsuario;
    }

    /** Ajuste solicitado pelo usuário: reaproveitar cookie de sessão após F5 (#28). */
    private boolean ensureAuthenticated(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        if (isAuthenticated(session) || isAuthenticatedSecurity()) {
            return true;
        }
        return rememberMeService.tryRestoreSession(request, response);
    }

    private void populateUserAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario user) {
            model.addAttribute("usuarioNome", user.getNome());
            model.addAttribute("usuarioTipo", user.getCargo().name());
        } else {
            model.addAttribute("usuarioNome", "Visitante");
            model.addAttribute("usuarioTipo", "VISITANTE");
        }
    }
}
