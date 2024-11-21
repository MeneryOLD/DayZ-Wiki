package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.service.user.UserAvatarService;
import com.dayzwiki.portal.service.user.UserBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserPersonalizationController {

    private final UserAvatarService userAvatarService;
    private final UserBannerService userBannerService;

    @PostMapping("/upload-avatar")
    @ResponseBody
    public ResponseEntity<String> uploadAvatar(@RequestParam("avatar") MultipartFile file, @RequestParam("user_id") long userId) {
        try {
            userAvatarService.uploadAvatar(file, userId);
            return ResponseEntity.ok("Avatar successfully uploaded!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading avatar.");
        }
    }

    @GetMapping("/get-avatar")
    @ResponseBody
    public ResponseEntity<byte[]> getAvatar(@RequestParam("user_id") long userId) {
        byte[] avatar = userAvatarService.getAvatar(userId);
        return avatar != null ? ResponseEntity.ok(avatar) : ResponseEntity.notFound().build();
    }

    @PostMapping("/upload-banner")
    @ResponseBody
    public ResponseEntity<String> uploadBanner(@RequestParam("banner") MultipartFile file, @RequestParam("user_id") long userId) {
        try {
            userBannerService.uploadBanner(file, userId);
            return ResponseEntity.ok("User banner successfully uploaded.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading banner.");
        }
    }

    @GetMapping("/get-banner")
    @ResponseBody
    public ResponseEntity<byte[]> getBanner(@RequestParam("user_id") long userId) {
        byte[] banner = userBannerService.getBanner(userId);
        return banner != null ? ResponseEntity.ok(banner) : ResponseEntity.notFound().build();
    }

}