package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.service.PostModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


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
    @ResponseBody
    public ResponseEntity<?> approvePost(@PathVariable Integer id) {
        postModerationService.approvePost(id);
        return ResponseEntity.ok("Post approve successfully");
    }

    @PostMapping("/reject/{id}")
    @ResponseBody
    public ResponseEntity<?> rejectPost(@PathVariable Integer id) {
        postModerationService.rejectPost(id);
        return ResponseEntity.ok("Post rejected successfully");
    }

}
