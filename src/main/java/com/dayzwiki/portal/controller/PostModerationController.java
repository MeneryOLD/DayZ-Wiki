package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.item.Post;
import com.dayzwiki.portal.model.item.PostImage;
import com.dayzwiki.portal.repository.item.PostImageRepository;
import com.dayzwiki.portal.repository.item.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/moder/posts")
public class PostModerationController {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    @GetMapping()
    public String getAllUnapprovedPost(Model model) {
        model.addAttribute("posts", postRepository.findAllByApproved(false));
        return "moder-posts";
    }

    @PostMapping("/approve/{id}")
    public String approvePost(@PathVariable("id") Integer id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            post.setApproved(true);
            postRepository.save(post);
        }
        return "redirect:/moder/posts";
    }

    @PostMapping("/reject/{id}")
    public String rejectPost(@PathVariable("id") Integer id) {
        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            postImageRepository.deleteAllByPostId(post.getId());
            postRepository.delete(post);
        }
        return "redirect:/moder/posts";
    }

}
