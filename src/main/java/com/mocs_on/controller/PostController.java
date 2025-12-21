package com.mocs_on.controller;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mocs_on.domain.Post;
import com.mocs_on.domain.Reaction;
import com.mocs_on.service.PostDAO;
import com.mocs_on.service.LoginDAO;
import com.mocs_on.domain.Usuario;

@RestController
@RequestMapping("/post")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostDAO postService;

    @Autowired
    private LoginDAO loginDAO;

    @GetMapping
    public ResponseEntity<List<Post>> recuperarAviso(
            @RequestParam(name = "usuario", required = false) String usuario,
            Principal principal) {
        return ResponseEntity.ok(postService.recuperarTodos());
    }

    @GetMapping("/gallery")
    public ResponseEntity<List<Post>> recuperarGaleria() {
        return ResponseEntity.ok(postService.recuperarGaleria());
    }

    @PostMapping
    public ResponseEntity<Void> postarAviso(@RequestBody Post post, Principal principal) {
        post.setData(LocalDateTime.now());

        // If creating a Consulta Informal, enforce delegate + director approval + committee set
        if (post.getStatus() == Post.TipoPost.CONSULTA_INFORMAL) {
            if (principal == null || principal.getName() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            var userOpt = loginDAO.findByEmail(principal.getName());
            if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            Usuario u = userOpt.get();
            if (u.getTipo() == null || !u.getTipo().name().equalsIgnoreCase("DELEGADO")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (post.getComiteSigla() == null || post.getComiteSigla().isBlank() || post.getAprovador() == null) {
                return ResponseEntity.badRequest().build();
            }

            var approverOpt = loginDAO.findByEmail(post.getAprovador());
            if (approverOpt.isEmpty() || approverOpt.get().getTipo() == null || !approverOpt.get().getTipo().name().equalsIgnoreCase("DIRETOR")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        Long id = postService.inserirPost(post);
        if (id != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();

            return ResponseEntity.created(location).build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{postId}/reaction")
    public ResponseEntity<Void> addReaction(
            @PathVariable Long postId,
            @RequestBody Reaction body,
            Principal principal
    ) {
        if (body == null || body.getEmoji() == null || body.getEmoji().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String usuario = (principal != null) ? principal.getName() : body.getUsuario();
        if (usuario == null || usuario.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String currentReaction = postService.findUserReaction(postId, usuario);

        if (currentReaction == null) {
            postService.addReactionToPost(postId, usuario, body.getEmoji());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        if (currentReaction.equals(body.getEmoji())) {
            postService.removeReactionFromPost(postId, usuario, body.getEmoji());
            return ResponseEntity.noContent().build();
        }

        postService.removeReactionFromPost(postId, usuario, currentReaction);
        postService.addReactionToPost(postId, usuario, body.getEmoji());
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{postId}/reaction")
    public ResponseEntity<Void> removeReaction(@PathVariable Long postId, @RequestBody Reaction body) {
        if (body == null || body.getEmoji() == null || body.getEmoji().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        int removed = postService.removeReactionFromPost(postId, body.getUsuario(), body.getEmoji());
        if (removed > 0) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/reactions")
    public ResponseEntity<Void> deleteAllReactions() {
        postService.deleteAllReactions();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{postId}/exclude")
    public ResponseEntity<Void> excludePost(@PathVariable Long postId) {
        int updated = postService.softDeletePost(postId);
        if (updated > 0) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
