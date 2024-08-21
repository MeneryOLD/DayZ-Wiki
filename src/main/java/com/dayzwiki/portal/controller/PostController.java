package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.item.Post;
import com.dayzwiki.portal.repository.item.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;

    @GetMapping
    public String getAllPosts() {
        return "posts";
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable("id") Integer id, Model model) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            return "post";
        }
        return "error";
    }

    // get post by id
    // get all posts
    // create post
    // delete post
    // update post
    // accept post
    // overrule post
}
