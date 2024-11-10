package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.PostImage;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    @GetMapping
    public String getAllApprovedPosts(Model model) {
        model.addAttribute("posts", postRepository.findAllByApproved(true));
        return "post/posts";
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable("id") Integer id, Model model, Principal principal) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isPresent()) {
            List<PostImage> postImages = postImageRepository.findAllByPostId(id);

            List<String> encodedImages = postImages.stream()
                    .map(image -> Base64.getEncoder().encodeToString(image.getImage()))
                    .collect(Collectors.toList());

            boolean isOwner = principal.getName().equals(post.get().getAuthor().getEmail());

            model.addAttribute("post", post.get());
            model.addAttribute("postImages", encodedImages);
            model.addAttribute("isOwner", isOwner);
            return "post/post";
        }
        return "error";
    }
}
