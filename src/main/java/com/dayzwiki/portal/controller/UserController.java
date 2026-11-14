package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import com.dayzwiki.portal.service.CurrentUserService;
import com.dayzwiki.portal.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/** Public profile pages: /@username */
@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

    private static final List<String> ROLE_ORDER = List.of("ADMIN", "MODER", "USER");

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final CurrentUserService currentUser;

    @GetMapping("/@{username}")
    public String handleUserRequest(Model model, @PathVariable String username, Principal principal) {
        Optional<User> found = userRepository.findByName(username);
        if (found.isEmpty()) {
            return "error";
        }
        User user = found.get();
        User viewer = currentUser.find(principal).orElse(null);
        boolean isOwner = CurrentUserService.sameUser(user, viewer);
        boolean moderator = CurrentUserService.isModerator(principal);

        // Visitors see approved posts; the owner and moderators also see posts waiting for review.
        List<Post> posts = postRepository.findAllByAuthorOrderByCreatedAtDesc(user).stream()
                .filter(p -> p.isApproved() || isOwner || moderator)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("posts", postService.cards(posts));
        model.addAttribute("approvedCount", posts.stream().filter(Post::isApproved).count());
        model.addAttribute("pendingCount", posts.stream().filter(p -> !p.isApproved()).count());
        model.addAttribute("roles", user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(Role::getName)
                .sorted((a, b) -> Integer.compare(rank(a), rank(b)))
                .toList());
        model.addAttribute("bans", user.getBans() == null ? List.of() : user.getBans());
        model.addAttribute("email", isOwner ? user.getEmail() : null);
        model.addAttribute("daysSurvived", user.getCreateDate() == null ? 0
                : Math.max(0, ChronoUnit.DAYS.between(user.getCreateDate(), LocalDateTime.now())));
        return "profile/user_profile";
    }

    private static int rank(String role) {
        int i = ROLE_ORDER.indexOf(role);
        return i < 0 ? ROLE_ORDER.size() : i;
    }
}
