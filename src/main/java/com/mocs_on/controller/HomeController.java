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
import com.mocs_on.dto.ComiteCatalogDTO;
import com.mocs_on.dto.ComiteDelegacaoDTO;
import com.mocs_on.dto.DelegacaoGrupoDTO;
import com.mocs_on.dto.DelegadoResumoDTO;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.AvisoDAO;
import com.mocs_on.service.ComiteDao;
import com.mocs_on.service.DocumentoDAO;
import com.mocs_on.service.GuiaEstudosDAO;
import com.mocs_on.service.PreRegistrationService;
import com.mocs_on.service.SecretariatDashboardService;
import com.mocs_on.service.SecretariatDashboardService.DashboardMetrics;
import com.mocs_on.service.UsuarioComiteDao;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private UsuarioComiteDao usuarioComiteDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
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
        populateAcademicDrawer(model, session);
        return "dashboard";
    }

    @GetMapping("/academic.html")
    public String academic(HttpSession session, Model model) {
        if (!isAuthenticated(session) && !isAuthenticatedSecurity()) {
            return "redirect:/login";
        }
        if (isSecretariat(session)) {
            return "redirect:/secretariado.html";
        }
        populateUserAttributes(model);
        populateAcademicDrawer(model, session);
        populateAcademicCatalog(model);
        return "academic";
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
        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario user) {
            model.addAttribute("usuarioNome", user.getNome());
            model.addAttribute("usuarioTipo", user.getCargo().name());
        } else {
            model.addAttribute("usuarioNome", "Visitante");
            model.addAttribute("usuarioTipo", "VISITANTE");
        }
    }

    private void populateAcademicDrawer(Model model, HttpSession session) {
        Long userId = currentUserId(session);
        if (userId == null || isSecretariat(session)) {
            model.addAttribute("academicComites", Collections.emptyList());
            return;
        }

        List<ComiteDelegacaoDTO> academicComites = usuarioComiteDao.listarComitesComDelegacao(userId);
        model.addAttribute("academicComites", academicComites);
        model.addAttribute("comiteLabel", formatComiteLabel(academicComites));
        model.addAttribute("delegacaoLabel", formatDelegacaoLabel(academicComites));
        model.addAttribute("delegacaoNotificacoes", fetchDelegacaoNotificacoes(userId));
        FaltasResumo faltas = fetchFaltasResumo(userId);
        model.addAttribute("faltasPercent", calcularPercentualFaltas(faltas));
        model.addAttribute("faltasTotal", faltas.total());
        model.addAttribute("faltasAusentes", faltas.ausentes());
    }

    private void populateAcademicCatalog(Model model) {
        List<ComiteCatalogDTO> catalogComites = fetchCatalogComites();
        model.addAttribute("catalogComites", catalogComites);
        model.addAttribute("delegacoesPorComite", fetchDelegacoesPorComite(catalogComites));
    }

    private List<ComiteCatalogDTO> fetchCatalogComites() {
        String sql = "SELECT id, nome, sigla, descricao, status, num_delegados FROM comites ORDER BY nome";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> new ComiteCatalogDTO(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("sigla"),
                    rs.getString("descricao"),
                    rs.getString("status"),
                    rs.getInt("num_delegados")
            ));
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private Map<Long, List<DelegacaoGrupoDTO>> fetchDelegacoesPorComite(List<ComiteCatalogDTO> comites) {
        Map<Long, Map<Long, DelegacaoGrupoDTO>> grouped = new LinkedHashMap<>();
        if (comites != null) {
            for (ComiteCatalogDTO comite : comites) {
                if (comite != null && comite.getId() != null) {
                    grouped.put(comite.getId(), new LinkedHashMap<>());
                }
            }
        }

        String delegacoesSql = "SELECT id, comite_id, nome FROM delegacoes ORDER BY nome";
        try {
            jdbcTemplate.query(delegacoesSql, rs -> {
                long comiteId = rs.getLong("comite_id");
                Long delegacaoId = rs.getLong("id");
                String nome = rs.getString("nome");
                Map<Long, DelegacaoGrupoDTO> byComite = grouped.computeIfAbsent(comiteId, key -> new LinkedHashMap<>());
                if (!byComite.containsKey(delegacaoId)) {
                    byComite.put(delegacaoId, new DelegacaoGrupoDTO(delegacaoId, nome));
                }
            });
        } catch (Exception ignored) {
        }

        String usuariosSql = """
                SELECT ud.comite_id, d.id AS delegacao_id, d.nome AS delegacao_nome,
                       u.id AS usuario_id, u.nome AS usuario_nome, u.email AS usuario_email
                FROM usuario_delegacao ud
                JOIN delegacoes d ON d.id = ud.delegacao_id
                JOIN usuarios u ON u.id = ud.usuario_id
                WHERE ud.delegacao_id IS NOT NULL
                ORDER BY d.nome, u.nome
                """;
        try {
            jdbcTemplate.query(usuariosSql, rs -> {
                long comiteId = rs.getLong("comite_id");
                long delegacaoId = rs.getLong("delegacao_id");
                String delegacaoNome = rs.getString("delegacao_nome");
                Long usuarioId = rs.getLong("usuario_id");
                String usuarioNome = rs.getString("usuario_nome");
                String usuarioEmail = rs.getString("usuario_email");

                Map<Long, DelegacaoGrupoDTO> byComite = grouped.computeIfAbsent(comiteId, key -> new LinkedHashMap<>());
                DelegacaoGrupoDTO grupo = byComite.get(delegacaoId);
                if (grupo == null) {
                    grupo = new DelegacaoGrupoDTO(delegacaoId, delegacaoNome);
                    byComite.put(delegacaoId, grupo);
                }
                grupo.addDelegado(new DelegadoResumoDTO(usuarioId, usuarioNome, usuarioEmail));
            });
        } catch (Exception ignored) {
        }

        Map<Long, List<DelegacaoGrupoDTO>> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Map<Long, DelegacaoGrupoDTO>> entry : grouped.entrySet()) {
            result.put(entry.getKey(), new ArrayList<>(entry.getValue().values()));
        }
        return result;
    }

    private Long currentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object idAttr = session.getAttribute(AuthController.SESSION_USER_ID);
        if (idAttr instanceof Number number) {
            return number.longValue();
        }
        try {
            return idAttr != null ? Long.parseLong(idAttr.toString()) : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isSecretariat(HttpSession session) {
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
        return normalized.equals("SECRETARIADO") || normalized.equals("SECRETARIO");
    }

    private String formatComiteLabel(List<ComiteDelegacaoDTO> comites) {
        if (comites == null || comites.isEmpty()) {
            return "Sem comite";
        }
        int limit = Math.min(3, comites.size());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(formatSingleComite(comites.get(i)));
        }
        int remaining = comites.size() - limit;
        if (remaining > 0) {
            builder.append(" +").append(remaining);
        }
        String label = builder.toString().trim();
        return label.isEmpty() ? "Sem comite" : label;
    }

    private String formatDelegacaoLabel(List<ComiteDelegacaoDTO> comites) {
        if (comites == null || comites.isEmpty()) {
            return "Sem delegacao";
        }
        StringBuilder builder = new StringBuilder();
        for (ComiteDelegacaoDTO comite : comites) {
            String comiteLabel = formatSingleComite(comite);
            if (builder.length() > 0) {
                builder.append(" | ");
            }
            String delegacao = safeTrim(comite.getDelegacaoNome());
            if (delegacao.isEmpty()) {
                builder.append(comiteLabel).append(": Sem delegacao");
            } else {
                builder.append(comiteLabel).append(": ").append(delegacao);
            }
        }
        String label = builder.toString().trim();
        return label.isEmpty() ? "Sem delegacao" : label;
    }

    private String formatSingleComite(ComiteDelegacaoDTO comite) {
        if (comite == null) {
            return "";
        }
        String sigla = safeTrim(comite.getSigla());
        String nome = safeTrim(comite.getNome());
        if (sigla.isEmpty()) {
            return nome;
        }
        if (nome.isEmpty()) {
            return sigla;
        }
        return sigla + " - " + nome;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private FaltasResumo fetchFaltasResumo(long userId) {
        String sql = """
                SELECT
                    COALESCE(SUM(CASE WHEN UPPER(status) = 'AUSENTE' THEN 1 ELSE 0 END), 0) AS ausentes,
                    COUNT(*) AS total
                FROM presenca_registros
                WHERE usuario_id = ?
                """;
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                long total = rs.getLong("total");
                long ausentes = rs.getLong("ausentes");
                return new FaltasResumo(total, ausentes);
            }
            return new FaltasResumo(0, 0);
        }, userId);
    }

    private Long calcularPercentualFaltas(FaltasResumo faltas) {
        if (faltas == null || faltas.total() <= 0) {
            return null;
        }
        return Math.round((faltas.ausentes() * 100.0) / faltas.total());
    }

    private List<String> fetchDelegacaoNotificacoes(Long userId) {
        String sql = "SELECT mensagem FROM delegacao_notificacoes " +
                "WHERE usuario_id = ? ORDER BY created_at DESC LIMIT 3";
        try {
            List<String> mensagens = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("mensagem"), userId);
            return mensagens == null ? Collections.emptyList() : mensagens;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private record FaltasResumo(long total, long ausentes) {
    }
}
