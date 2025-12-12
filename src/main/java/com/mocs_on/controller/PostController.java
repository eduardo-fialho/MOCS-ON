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
import com.mocs_on.domain.PostComment;
import com.mocs_on.domain.Reaction;
import com.mocs_on.service.PostDAO;

@RestController
@RequestMapping("/post")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostDAO postService;

    @GetMapping
    public ResponseEntity<List<Post>> recuperarAviso(
            @RequestParam(name = "usuario", required = false) String usuario,
            Principal principal) {

        String effectiveUser = (principal != null) ? principal.getName() : usuario;

        List<Post> posts;
        if (effectiveUser != null && !effectiveUser.isBlank()) {
            posts = postService.recuperarTodosParaUsuario(effectiveUser);
        } else {
            posts = postService.recuperarTodos();
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/gallery")
    public ResponseEntity<List<Post>> recuperarGaleria() {
        return ResponseEntity.ok(postService.recuperarGaleria());
    }

    @PostMapping
    public ResponseEntity<Void> postarAviso(@RequestBody Post post) {
        post.setData(LocalDateTime.now());

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
    public ResponseEntity<Void> addReaction(@PathVariable Long postId, @RequestBody Reaction body, Principal principal) {
        if (body == null || body.getEmoji() == null || body.getEmoji().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String usuario = (principal != null) ? principal.getName() : body.getUsuario();
        if (usuario == null || usuario.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        PostDAO.ReactionResult result = postService.reactToPost(postId, usuario, body.getEmoji());

        switch (result) {
            case CREATED:
                return ResponseEntity.status(HttpStatus.CREATED).build();
            case UPDATED:
                return ResponseEntity.ok().build();
            case REMOVED:
                return ResponseEntity.noContent().build();
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    /** Zera todas as reações (likes) de todos os posts. */
    @DeleteMapping("/reactions")
    public ResponseEntity<Void> deleteAllReactions() {
        postService.deleteAllReactions();
        return ResponseEntity.noContent().build();
    }

    /** Ajuste solicitado pelo usuário: permitir comentários individuais nas fotos da curadoria. */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<PostComment>> listComments(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.listComments(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Void> addComment(@PathVariable Long postId, @RequestBody CommentRequest body) {
        if (body == null || body.mensagem() == null || body.mensagem().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String autor = sanitize(body.autor(), "Delegado");
        String mensagem = sanitize(body.mensagem(), null);
        postService.addComment(postId, autor, mensagem);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private String sanitize(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue == null ? "" : defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

    private record CommentRequest(String autor, String mensagem) {}

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
