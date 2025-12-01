package com.mocs_on.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.HtmlUtils;

import com.mocs_on.auth.EmailService;
import com.mocs_on.domain.OuvidoriaRelato;
import com.mocs_on.service.OuvidoriaDAO;
import com.mocs_on.service.SecretariadoDAO;

@Controller
@RequestMapping("/ouvidoria")
public class OuvidoriaController {

    @Autowired
    private OuvidoriaDAO ouvidoriaDAO;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SecretariadoDAO secretariadoDAO;

    @Value("${spring.mail.username:projectmocs@gmail.com}")
    private String secretariadoEmail;

    @PostMapping("/submit")
    public String submit(
            @RequestParam(name = "identificacao", required = true) String identificacao,
            @RequestParam(name = "categoria_relato", required = true) String categoriaRelato,
            @RequestParam(name = "nome_relator", required = false) String nomeRelator,
            @RequestParam(name = "comite_relator", required = false) String comiteRelator,

            @RequestParam(name = "comite_conducao", required = false) String comiteConducao,
            @RequestParam(name = "comite_respeito", required = false) String comiteRespeito,
            @RequestParam(name = "comite_imparcialidade", required = false) String comiteImparcialidade,
            @RequestParam(name = "comite_apoio", required = false) String comiteApoio,
            @RequestParam(name = "comite_mensagem", required = false) String comiteMensagem,

            @RequestParam(name = "secretariado_positivos", required = false) String secretPos,
            @RequestParam(name = "secretariado_negativos", required = false) String secretNeg,
            @RequestParam(name = "secretariado_falta", required = false) String secretFalta,
            @RequestParam(name = "secretariado_sugestoes", required = false) String secretSugest,

            @RequestParam(name = "outros_mensagem", required = false) String outrosMensagem,
            RedirectAttributes redirect
    ) {

        if (identificacao == null || identificacao.isBlank() || categoriaRelato == null || categoriaRelato.isBlank()) {
            redirect.addAttribute("error", "1");
            return "redirect:/ouvidoria.html";
        }

        OuvidoriaRelato r = new OuvidoriaRelato();
        r.setIdentificacao(identificacao);
        r.setCategoriaRelato(categoriaRelato);
        r.setNomeRelator(nomeRelator);
        r.setComiteRelator(comiteRelator);

        r.setComiteConducao(comiteConducao);
        r.setComiteRespeito(comiteRespeito);
        r.setComiteImparcialidade(comiteImparcialidade);
        r.setComiteApoio(comiteApoio);
        r.setComiteMensagem(comiteMensagem);

        r.setSecretariadoPositivos(secretPos);
        r.setSecretariadoNegativos(secretNeg);
        r.setSecretariadoFalta(secretFalta);
        r.setSecretariadoSugestoes(secretSugest);

        r.setOutrosMensagem(outrosMensagem);

        try {
            Long id = ouvidoriaDAO.inserirRelato(r);

            // build HTML email body
            StringBuilder html = new StringBuilder();
            html.append("<html><body>");
            html.append("<h2>Novo relato de Ouvidoria</h2>");
            html.append("<p><strong>Identificação:</strong> ").append(escapeHtml(identificacao)).append("</p>");
            if (nomeRelator != null && !nomeRelator.isBlank()) html.append("<p><strong>Nome do relator:</strong> ").append(escapeHtml(nomeRelator)).append("</p>");
            if (comiteRelator != null && !comiteRelator.isBlank()) html.append("<p><strong>Comitê do relator:</strong> ").append(escapeHtml(comiteRelator)).append("</p>");
            html.append("<p><strong>Categoria:</strong> ").append(escapeHtml(categoriaRelato)).append("</p>");

            html.append("<h3>Feedback Comitê</h3>");
            if (comiteConducao != null) html.append("<p><strong>Condução:</strong> ").append(escapeHtml(comiteConducao)).append("</p>");
            if (comiteRespeito != null) html.append("<p><strong>Respeito:</strong> ").append(escapeHtml(comiteRespeito)).append("</p>");
            if (comiteImparcialidade != null) html.append("<p><strong>Imparcialidade:</strong> ").append(escapeHtml(comiteImparcialidade)).append("</p>");
            if (comiteApoio != null) html.append("<p><strong>Apoio:</strong> ").append(escapeHtml(comiteApoio)).append("</p>");
            if (comiteMensagem != null) html.append("<p><strong>Comentários:</strong><br>").append(nl2br(escapeHtml(comiteMensagem))).append("</p>");

            html.append("<h3>Feedback Secretariado</h3>");
            if (secretPos != null) html.append("<p><strong>Pontos positivos:</strong><br>").append(nl2br(escapeHtml(secretPos))).append("</p>");
            if (secretNeg != null) html.append("<p><strong>Pontos negativos:</strong><br>").append(nl2br(escapeHtml(secretNeg))).append("</p>");
            if (secretFalta != null) html.append("<p><strong>O que faltou:</strong><br>").append(nl2br(escapeHtml(secretFalta))).append("</p>");
            if (secretSugest != null) html.append("<p><strong>Sugestões:</strong><br>").append(nl2br(escapeHtml(secretSugest))).append("</p>");

            if (outrosMensagem != null) html.append("<h3>Outros</h3><p>").append(nl2br(escapeHtml(outrosMensagem))).append("</p>");

            // admin link to view relato
            if (id != null) {
                html.append("<p>Ver relato (admin): <a href=\"/ouvidoria/admin/").append(id).append("\">/ouvidoria/admin/").append(id).append("</a></p>");
            }
            html.append("</body></html>");

            // send notification to all secretariado emails
            try {
                String subject = "Novo relato de Ouvidoria - MOCS ON";
                java.util.List<String> emails = secretariadoDAO.listarEmails();
                if (emails == null || emails.isEmpty()) {
                    // fallback to configured single address
                    emailService.send(secretariadoEmail, subject, html.toString(), true);
                } else {
                    for (String to : emails) {
                        try {
                            emailService.send(to, subject, html.toString(), true);
                        } catch (Exception e) {
                            // continue other recipients
                        }
                    }
                }
            } catch (Exception ex) {
                // swallow - fallback handled inside EmailService
            }

            if (id != null) {
                redirect.addAttribute("success", "1");
            } else {
                redirect.addAttribute("error", "2");
            }
        } catch (Exception e) {
            redirect.addAttribute("error", "2");
        }

        return "redirect:/ouvidoria.html";
    }

    @GetMapping("/admin/list")
    public String adminList(Model model) {
        java.util.List<OuvidoriaRelato> list = ouvidoriaDAO.recuperarTodos();
        model.addAttribute("relatos", list);
        return "ouvidoria_list";
    }

    @GetMapping("/admin/{id}")
    public String adminView(@PathVariable Long id, Model model) {
        OuvidoriaRelato r = ouvidoriaDAO.recuperarPorId(id);
        if (r == null) {
            return "redirect:/ouvidoria/admin/list";
        }
        model.addAttribute("relato", r);
        return "ouvidoria_view";
    }

    private String escapeHtml(String s) {
        return s == null ? "" : HtmlUtils.htmlEscape(s);
    }

    private String nl2br(String s) {
        return s == null ? "" : s.replace("\n", "<br/>");
    }
}
