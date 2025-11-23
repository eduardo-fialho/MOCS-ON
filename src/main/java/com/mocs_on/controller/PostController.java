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

import com.mocs_on.domain.Reaction;
import com.mocs_on.domain.Post;
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
