package com.mocs_on.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mocs_on.domain.ListaPresenca;
import com.mocs_on.domain.RegistroPresenca;
import com.mocs_on.dto.ComiteResumoDTO;
import com.mocs_on.dto.PresencaDetalheDTO;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.PresencaService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/presencas")
public class PresencaController {

    private final PresencaService presencaService;

    public PresencaController(PresencaService presencaService) {
        this.presencaService = presencaService;
    }

    @GetMapping("/comites")
    public ResponseEntity<List<ComiteResumoDTO>> listarComites(HttpSession session) {
        if (!canAccess(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(presencaService.listarComites());
    }

    @GetMapping("/listas")
    public ResponseEntity<List<ListaPresenca>> listarListas(HttpSession session) {
        if (!canAccess(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(presencaService.listarListas());
    }

    @GetMapping("/listas/{id}")
    public ResponseEntity<PresencaDetalheDTO> obterDetalhe(@PathVariable("id") Long id, HttpSession session) {
        if (!canAccess(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PresencaDetalheDTO detalhe = presencaService.obterDetalhe(id);
        if (detalhe == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detalhe);
    }

    @PostMapping("/listas")
    public ResponseEntity<?> criarLista(@RequestBody ListaPresenca request, HttpSession session) {
        if (!canManage(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
        }
        String titulo = trimToNull(request.getTitulo());
        String dataSessao = trimToNull(request.getDataSessao());
        if (!StringUtils.hasText(titulo) || !StringUtils.hasText(dataSessao)) {
            return ResponseEntity.badRequest().body("Titulo e data da sessao sao obrigatorios.");
        }

        ListaPresenca lista = new ListaPresenca();
        lista.setTitulo(titulo);
        lista.setDataSessao(dataSessao);
        lista.setHoraInicio(trimToNull(request.getHoraInicio()));
        lista.setHoraFim(trimToNull(request.getHoraFim()));
        lista.setObservacao(trimToNull(request.getObservacao()));
        lista.setComiteId(request.getComiteId());
        lista.setCriadoPor(resolveUserName(session));

        ListaPresenca criada = presencaService.criarLista(lista);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/listas/{id}/registros")
    public ResponseEntity<String> atualizarRegistros(@PathVariable("id") Long id,
                                                     @RequestBody RegistrosRequest payload,
                                                     HttpSession session) {
        if (!canManage(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
        }
        if (payload == null || payload.getRegistros() == null) {
            return ResponseEntity.badRequest().body("Registros ausentes.");
        }
        presencaService.atualizarRegistros(id, payload.getRegistros());
        return ResponseEntity.ok("Registros atualizados.");
    }

    private boolean canAccess(HttpSession session) {
        return isAuthenticated(session) && hasPresenceRole(session);
    }

    private boolean canManage(HttpSession session) {
        return canAccess(session);
    }

    private boolean isAuthenticated(HttpSession session) {
        if (session != null && session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE) != null) {
            return true;
        }
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

    private String resolveUserName(HttpSession session) {
        if (session != null) {
            Object nameAttr = session.getAttribute(AuthController.SESSION_USER_NAME);
            if (nameAttr != null) {
                return nameAttr.toString();
            }
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario user) {
            return user.getNome();
        }
        return "Sistema";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static class RegistrosRequest {
        private List<RegistroPresenca> registros;

        public List<RegistroPresenca> getRegistros() {
            return registros;
        }

        public void setRegistros(List<RegistroPresenca> registros) {
            this.registros = registros;
        }
    }
}
