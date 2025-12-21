package com.mocs_on.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mocs_on.domain.Documento;
import com.mocs_on.domain.GuiaEstudos;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.AvisoDAO;
import com.mocs_on.service.ComiteDao;
import com.mocs_on.service.DocumentoDAO;
import com.mocs_on.service.GuiaEstudosDAO;
import com.mocs_on.service.PreRegistrationService;
import com.mocs_on.service.SecretariatDashboardService;
import com.mocs_on.service.SecretariatDashboardService.DashboardMetrics;

import jakarta.servlet.http.HttpSession;

@Controller
@CrossOrigin(origins = "*")
public class HomeController {

    @Autowired
    private DocumentoDAO documentoDAO;
    @Autowired
    private AvisoDAO avisoDAO;
    @Autowired
    private PreRegistrationService preRegistrationService;
    @Autowired
    private SecretariatDashboardService secretariatDashboardService;
    @Autowired
    private GuiaEstudosDAO guiasService;
    @Autowired
    private ComiteDao comiteService;

    @GetMapping({"", "/"})
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        if (session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null) {
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
    public String dashboard(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "dashboard";
    }

    @GetMapping("/mesa_diretora.html")
    public String mesaDiretora(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "mesa_diretora";
    }

    @GetMapping("/secretariado.html")
    public String secretariado(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        DashboardMetrics metrics = secretariatDashboardService.collectMetrics();
        model.addAttribute("dashboardMetrics", metrics);
        model.addAttribute("numAvisos", avisoDAO.quantidadeAvisos());
        model.addAttribute("numDocumentos", documentoDAO.quantidadeDocumentos());
        model.addAttribute("pendingPreCount", preRegistrationService.countPending());
        populateUserAttributes(model);
        return "secretariado";
    }

    @GetMapping("/documentos.html")
    public String documentos(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "documentos";
    }

    @GetMapping("/guias_de_estudos.html")
    public String guiaEstudos(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "painel_guia_de_estudos";
    }

    @GetMapping("/presencas.html")
    public String presencas(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        if (!hasPresenceRole(session)) {
            return "redirect:/dashboard.html";
        }
        populateUserAttributes(model);
        return "presencas";
    }

    @GetMapping("/avaliar_documentos.html")
    public String avaliar(@RequestParam(required = false) Long docId, HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        if (docId != null) {
            Documento doc = documentoDAO.recuperarPorId(docId);
            model.addAttribute("doc", doc);
        }
        return "avaliar_documentos";
    }

    @GetMapping("/submissao_documentos.html")
    public String submeterDocumento(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "submissao_documentos";
    }

    @GetMapping("/cadastrar_guia.html")
    public String cadastrarGuiaDeEstudos(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "cadastrar_guia";
    }

    @GetMapping("/editar_guia.html")
    public String editarGuiaDeEstudos(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        populateUserAttributes(model);
        return "editar_guia_de_estudos";
    }

    @GetMapping("/guia_estudo.html")
    public String visualizarGuia(@RequestParam Long id, HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        model.addAttribute("id", id);
        populateUserAttributes(model);
        return "guia_de_estudos";
    }

    @GetMapping("/editar_guia_de_estudos.html")
    public String editarGuiaDeEstudos(@RequestParam Long id, HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        GuiaEstudos guia = guiasService.recuperarPorId(id);
        if (guia == null) {
            return "redirect:/guias_de_estudos.html";
        }
        model.addAttribute("guia", guia);
        model.addAttribute("comites", comiteService.informacoesComites());
        populateUserAttributes(model);
        return "editar_guia_de_estudos";
    }

    private boolean isAuthenticated(HttpSession session) {
        return session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null;
    }

    private boolean isAuthenticatedSecurity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof SecaoUsuario;
    }

    private boolean hasPresenceRole(HttpSession session) {
        String role = null;
        if (session != null) {
            Object roleAttr = session.getAttribute(AuthController.SESSION_USER_ROLE);
            if (roleAttr != null) {
                role = roleAttr.toString();
            }
        }
        if (!StringUtils.hasText(role)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario user) {
                role = user.getCargo().name();
            }
        }
        if (!StringUtils.hasText(role)) {
            return false;
        }
        String normalized = role.trim().toUpperCase();
        return normalized.equals("SECRETARIADO")
                || normalized.equals("SECRETARIO")
                || normalized.equals("EQUIPE")
                || normalized.equals("DIRETOR");
    }

    private void populateUserAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario) {
            SecaoUsuario user = (SecaoUsuario) authentication.getPrincipal();
            model.addAttribute("usuarioNome", user.getNome());
            model.addAttribute("usuarioTipo", user.getCargo().name());
        } else {
            model.addAttribute("usuarioNome", "Visitante");
            model.addAttribute("usuarioTipo", "VISITANTE");
        }
    }
}
