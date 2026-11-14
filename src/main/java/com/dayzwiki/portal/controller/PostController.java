package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.PostImage;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.service.CurrentUserService;
import com.dayzwiki.portal.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** Community posts: the list, a single post and post images. */
@RequiredArgsConstructor
@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostService postService;
    private final CurrentUserService currentUser;

    @GetMapping
    public String getAllApprovedPosts(Model model, Principal principal) {
        Optional<User> viewer = currentUser.find(principal);
        boolean moderator = CurrentUserService.isModerator(principal);
        model.addAttribute("posts", postService.cards(postRepository.findAllByApprovedOrderByCreatedAtDesc(true)));
        model.addAttribute("signedIn", viewer.isPresent());
        model.addAttribute("pendingCount", moderator ? postRepository.countByApproved(false) : 0);
        model.addAttribute("myPending", viewer.map(u -> postRepository.countByAuthorAndApproved(u, false)).orElse(0L));
        model.addAttribute("isModerator", moderator);
        return "post/posts";
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable("id") Integer id, Model model, Principal principal) {
        Optional<Post> found = postRepository.findById(id);
        User viewer = currentUser.find(principal).orElse(null);
        boolean moderator = CurrentUserService.isModerator(principal);
        if (found.isEmpty() || !PostService.canView(found.get(), viewer, moderator)) {
            return "error";
        }
        Post post = found.get();
        model.addAttribute("post", post);
        model.addAttribute("imageIds", postImageRepository.findIdsByPostId(id));
        model.addAttribute("isOwner", CurrentUserService.sameUser(post.getAuthor(), viewer));
        model.addAttribute("canEdit", PostService.canEdit(post, viewer, moderator));
        model.addAttribute("isModerator", moderator);
        model.addAttribute("edited", post.getUpdatedAt() != null && post.getCreatedAt() != null
                && post.getUpdatedAt().isAfter(post.getCreatedAt().plusMinutes(1)));
        return "post/post";
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Integer imageId, Principal principal) {
        Optional<PostImage> image = postImageRepository.findById(imageId);
        if (image.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Post post = image.get().getPost();
        boolean visible = post.isApproved()
                || PostService.canView(post, currentUser.find(principal).orElse(null), CurrentUserService.isModerator(principal));
        if (!visible) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = image.get().getImage();
        String type = PostService.contentType(bytes);
        return ResponseEntity.ok()
                .contentType(type != null ? MediaType.parseMediaType(type) : MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(post.isApproved() ? CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic() : CacheControl.noStore())
                .body(bytes);
    }
}
