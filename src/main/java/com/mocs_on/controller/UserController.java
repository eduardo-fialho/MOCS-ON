package com.mocs_on.controller;

import com.mocs_on.auth.UserAccountService;
import com.mocs_on.domain.Post;
import com.mocs_on.domain.Usuario;
import com.mocs_on.dto.InformacoesUsuarioDTO;
import com.mocs_on.dto.UserSearchResultDTO;
import com.mocs_on.security.SecaoUsuario;
import com.mocs_on.service.LoginDAO;
import com.mocs_on.service.PostDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final LoginDAO loginDAO;
    private final PostDAO postDAO;
    private final UserAccountService userAccountService;

    public UserController(LoginDAO loginDAO, PostDAO postDAO, UserAccountService userAccountService) {
        this.loginDAO = loginDAO;
        this.postDAO = postDAO;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/user")
    public InformacoesUsuarioDTO getUserInfo(HttpSession session) {
        if (session != null) {
            Object nameAttr = session.getAttribute(AuthController.SESSION_USER_NAME);
            Object roleAttr = session.getAttribute(AuthController.SESSION_USER_ROLE);
            Object emailAttr = session.getAttribute(AuthController.SESSION_USER_ATTRIBUTE);
            if (nameAttr != null) {
                boolean isSecretariado = isSecretariatRole(roleAttr != null ? roleAttr.toString() : null);
                String email = emailAttr != null ? emailAttr.toString() : null;
                return new InformacoesUsuarioDTO(nameAttr.toString(), email, isSecretariado);
            }
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof SecaoUsuario) {
            SecaoUsuario user = (SecaoUsuario) authentication.getPrincipal();
            boolean isSecretario = isSecretariatRole(user.getCargo().name());
            return new InformacoesUsuarioDTO(user.getNome(), user.getUsername(), isSecretario);
        }

        return new InformacoesUsuarioDTO("Usuário Desconhecido", null, false);
    }

    @GetMapping("/user/search")
    public List<UserSearchResultDTO> searchUsers(@RequestParam("q") String query) {
        String normalized = query == null ? "" : query.trim();

        if (normalized.length() < 2) {
            return Collections.emptyList();
        }

        List<Usuario> usuarios = loginDAO.searchUsers(normalized, 8);

        return usuarios.stream()
                .map(usuario -> {
                    Optional<Post> latest = postDAO.findLatestVisiblePostByAuthor(usuario.getNome());
                    String snippet = latest.map(Post::getMensagem)
                            .map(this::truncateSnippet)
                            .orElse(null);
                    String date = latest.map(Post::getData)
                            .map(data -> data != null ? data.toString() : null)
                            .orElse(null);

                    String tipo = usuario.getTipo() != null ? usuario.getTipo().name() : "DELEGADO";

                    return new UserSearchResultDTO(
                            usuario.getId(),
                            usuario.getNome(),
                            usuario.getEmail(),
                            tipo,
                            snippet,
                            date
                    );
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/user/avatar")
    public ResponseEntity<Resource> getAvatarByName(@RequestParam("name") String name) {
        if (!StringUtils.hasText(name)) {
            return ResponseEntity.notFound().build();
        }

        Optional<UserAccountService.UserPhoto> photoOpt = userAccountService.findProfilePhotoByName(name.trim());
        if (photoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.IMAGE_PNG;
        String contentType = photoOpt.get().contentType();
        if (StringUtils.hasText(contentType)) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        Resource resource = new ByteArrayResource(photoOpt.get().data());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .contentType(mediaType)
                .body(resource);
    }

    private boolean isSecretariatRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toUpperCase();
        return normalized.equals("SECRETARIADO") || normalized.equals("SECRETARIO");
    }

    private String truncateSnippet(String mensagem) {
        if (mensagem == null) {
            return null;
        }
        return mensagem.length() > 140 ? mensagem.substring(0, 137) + "..." : mensagem;
    }
}
