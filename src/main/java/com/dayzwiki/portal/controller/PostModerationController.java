package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.service.PostModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RequiredArgsConstructor
@Controller
@RequestMapping("/moder/posts")
public class PostModerationController {

    private final PostModerationService postModerationService;

    @GetMapping()
    public String getAllUnapprovedPost(Model model) {
        model.addAttribute("posts", postModerationService.getAllUnapprovedPost());
        return "post/moder_posts";
    }

    @PostMapping("/approve/{id}")
    public String approvePost(@PathVariable Integer id) {
        postModerationService.approvePost(id);
        return "redirect:/moder/posts";
    }

    @PostMapping("/reject/{id}")
    public String rejectPost(@PathVariable Integer id) {
        postModerationService.rejectPost(id);
        return "redirect:/moder/posts";
    }

}
