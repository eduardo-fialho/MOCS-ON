
package com.mocs_on.controller;

import java.net.URI;
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
    public ResponseEntity<List<Post>> recuperarAviso() {
        List<Post> avisos = postService.recuperarTodos();
        return ResponseEntity.ok(avisos);
    }

    @PostMapping
    public ResponseEntity<Void> postarAviso(@RequestBody Post post) {
        post.setData(LocalDateTime.now());

        Long id = postService.inserirPost(post);
        if (id != null) {
            // cria Location: /post/{id}
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
    public ResponseEntity<Void> addReaction(@PathVariable Long postId, @RequestBody Reaction body) {
        if (body == null || body.getEmoji() == null || body.getEmoji().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        int result = postService.addReactionToPost(postId, body.getUsuario(), body.getEmoji());
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
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
}
