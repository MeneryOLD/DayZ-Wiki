package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.service.PostModerationService;
import com.dayzwiki.portal.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Controller
@RequestMapping("/moder/posts")
public class PostModerationController {

    private final PostModerationService postModerationService;
    private final PostService postService;
    private final PostImageRepository postImageRepository;

    @GetMapping()
    public String getAllUnapprovedPost(Model model) {
        // Oldest first: the queue is worked through in the order posts arrived.
        List<Post> posts = postModerationService.getAllUnapprovedPost().stream()
                .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Map<Integer, List<Integer>> imageIds = posts.stream()
                .collect(Collectors.toMap(Post::getId, p -> postImageRepository.findIdsByPostId(p.getId())));
        model.addAttribute("posts", postService.cards(posts));
        model.addAttribute("imageIds", imageIds);
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
