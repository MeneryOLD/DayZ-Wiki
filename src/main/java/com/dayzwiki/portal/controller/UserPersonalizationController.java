package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.service.CurrentUserService;
import com.dayzwiki.portal.service.user.UserAvatarService;
import com.dayzwiki.portal.service.user.UserBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Optional;

/** Profile pictures and banners. Anyone can view them; only the account owner can change them. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserPersonalizationController {

    private static final long MAX_BYTES = 8L * 1024 * 1024;

    private final UserAvatarService userAvatarService;
    private final UserBannerService userBannerService;
    private final CurrentUserService currentUser;

    @PostMapping("/upload-avatar")
    @ResponseBody
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file, Principal principal) {
        Optional<User> user = currentUser.find(principal);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in first.");
        }
        if (file.isEmpty() || file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body("Choose a JPG or PNG image up to 8 MB.");
        }
        try {
            userAvatarService.uploadAvatar(file, user.get().getId());
            return ResponseEntity.ok("Avatar successfully uploaded!");
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body("Choose a JPG or PNG image up to 8 MB.");
        }
    }

    @GetMapping("/get-avatar")
    @ResponseBody
    public ResponseEntity<byte[]> getAvatar(@RequestParam("user_id") long userId) {
        byte[] avatar = userAvatarService.getAvatar(userId);
        if (avatar == null) {
            // No picture yet: send the default one instead of a 404, so every <img> just works.
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/img/default-avatar.webp")).build();
        }
        return image(avatar);
    }

    @PostMapping("/upload-banner")
    @ResponseBody
    public ResponseEntity<String> uploadBanner(@RequestParam("banner") MultipartFile file, Principal principal) {
        Optional<User> user = currentUser.find(principal);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sign in first.");
        }
        if (file.isEmpty() || file.getSize() > MAX_BYTES) {
            return ResponseEntity.badRequest().body("Choose a JPG or PNG image up to 8 MB.");
        }
        try {
            userBannerService.uploadBanner(file, user.get().getId());
            return ResponseEntity.ok("User banner successfully uploaded.");
        } catch (IOException | RuntimeException e) {
            return ResponseEntity.badRequest().body("Choose a JPG or PNG image up to 8 MB.");
        }
    }

    @GetMapping("/get-banner")
    @ResponseBody
    public ResponseEntity<byte[]> getBanner(@RequestParam("user_id") long userId) {
        return image(userBannerService.getBanner(userId));
    }

    private static ResponseEntity<byte[]> image(byte[] bytes) {
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }
        // Stored as JPEG by the avatar/banner services. Short cache so a new picture shows up quickly.
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noCache())
                .body(bytes);
    }
}
