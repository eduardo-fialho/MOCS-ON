package com.mocs_on.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mocs_on.domain.Comment;
import com.mocs_on.domain.Usuario;
import com.mocs_on.security.CargoEnum;
import com.mocs_on.service.CommentDAO;
import com.mocs_on.service.LoginDAO;
import com.mocs_on.service.PostDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private LoginDAO loginDAO;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(
            @PathVariable Long postId,
            @RequestParam(name = "limit", required = false, defaultValue = "200") int limit,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {

        List<Comment> comments = commentDAO.getCommentsForPost(postId, limit, offset);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body,
            Principal principal,
            Authentication authentication) {

        if (body == null || body.get("mensagem") == null || body.get("mensagem").isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String usuario = null;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            usuario = principal.getName();
        } else if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            usuario = authentication.getName();
        } else {
            usuario = body.get("usuario");
        }

        if (usuario == null || usuario.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String usuarioNome = null;
        try {
            var opt = loginDAO.findByEmail(usuario);
            if (opt.isPresent()) {
                Usuario u = opt.get();
                usuarioNome = u.getNome();
            }
        } catch (Exception e) {
            usuarioNome = null;
        }

        String mensagem = body.get("mensagem").trim();

        try {
            Long id = commentDAO.addCommentToPost(postId, usuario, usuarioNome, mensagem);
            if (id != null) {
                Map<String, Object> out = new HashMap<>();
                out.put("id", id);
                out.put("createdAt", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.CREATED).body(out);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{postId}/comments/{commentId}/exclude")
    public ResponseEntity<Void> excludeComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Principal principal,
            Authentication authentication) {

        Comment comment = commentDAO.findById(commentId);
        if (comment == null || !comment.getPostId().equals(postId)) {
            return ResponseEntity.notFound().build();
        }

        if ((principal == null || principal.getName() == null) && (authentication == null || authentication.getName() == null)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String requester = (principal != null && principal.getName() != null) ? principal.getName() : authentication.getName();

        boolean isOwner = requester.equalsIgnoreCase(comment.getUsuario());

        boolean isSecretary = false;
        try {
            if (authentication != null && authentication.getAuthorities() != null) {
                for (GrantedAuthority a : authentication.getAuthorities()) {
                    if (a != null && "ROLE_SECRETARIADO".equalsIgnoreCase(a.getAuthority())) {
                        isSecretary = true;
                        break;
                    }
                }
            }

            if (!isSecretary) {
                var opt = loginDAO.findByEmail(requester);
                if (opt.isPresent()) {
                    Usuario u = opt.get();
                    CargoEnum cargo = u.getTipo();
                    if (cargo == CargoEnum.SECRETARIADO) {
                        isSecretary = true;
                    }
                }
            }
        } catch (Exception e) {
            isSecretary = false;
        }

        if (!isOwner && !isSecretary) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        int updated = commentDAO.softDeleteComment(commentId);
        if (updated > 0) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
