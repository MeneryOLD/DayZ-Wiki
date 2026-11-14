package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.PostDto;
import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.service.CurrentUserService;
import com.dayzwiki.portal.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

/** Create, edit and delete posts. Only the author or a moderator may change a post. */
@RequiredArgsConstructor
@Controller
@RequestMapping("/posts")
public class PostCrudController {

    private final PostRepository postRepository;
    private final PostService postService;
    private final CurrentUserService currentUser;

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createPost(@ModelAttribute PostDto postDto, Principal principal) {
        Optional<User> user = currentUser.find(principal);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Sign in to write a post."));
        }
        try {
            Post post = postService.create(user.get(), postDto.getTitle(), postDto.getContent(), postDto.getImages());
            return ResponseEntity.ok(Map.of("id", post.getId(), "message", "Post sent for review."));
        } catch (PostService.InvalidPostException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<?> editPost(@PathVariable Integer id, @ModelAttribute PostDto postDto, Principal principal) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Post not found."));
        }
        boolean moderator = CurrentUserService.isModerator(principal);
        if (!PostService.canEdit(post.get(), currentUser.find(principal).orElse(null), moderator)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can only edit your own posts."));
        }
        try {
            Post saved = postService.edit(post.get(), postDto.getTitle(), postDto.getContent(),
                    postDto.getKeepImageIds(), postDto.getImages(), moderator);
            return ResponseEntity.ok(Map.of("id", saved.getId(), "approved", saved.isApproved()));
        } catch (PostService.InvalidPostException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePost(@PathVariable Integer id, Principal principal) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (!PostService.canEdit(post.get(), currentUser.find(principal).orElse(null), CurrentUserService.isModerator(principal))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        postService.delete(post.get());
        return ResponseEntity.ok().build();
    }
}
