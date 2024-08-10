package com.dayzwiki.portal.controller.item;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/posts")
public class PostController {

    @GetMapping
    public String getAllPosts() {
        return "posts";
    }

    // get post by id
    // get all posts
    // create post
    // delete post
    // update post
    // accept post
    // overrule post
}
