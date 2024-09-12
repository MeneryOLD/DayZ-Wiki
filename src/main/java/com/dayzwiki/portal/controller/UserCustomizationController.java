package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.api.item.UserAvatar;
import com.dayzwiki.portal.dto.api.item.UserBanner;
import com.dayzwiki.portal.repository.user.UserAvatarRepository;
import com.dayzwiki.portal.repository.user.UserBannerRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class UserCustomizationController {

    private final UserAvatarRepository userAvatarRepository;
    private final UserBannerRepository userBannerRepository;

    @PostMapping("/upload-avatar")
    @ResponseBody
    public ResponseEntity<String> handleFileUpload(@RequestParam("avatar") MultipartFile file, @RequestParam("user_id") long userId) {
        try {
            InputStream inputStream = file.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(inputStream)
                    .size(512, 512)
                    .keepAspectRatio(true)
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            byte[] imageBytes = outputStream.toByteArray();

            UserAvatar userAvatar = userAvatarRepository.findByUserId(userId);
            if (userAvatar == null) {
                userAvatar = new UserAvatar();
                userAvatar.setUserId(userId);
            }

            userAvatar.setAvatar(imageBytes);
            userAvatarRepository.save(userAvatar);

            return ResponseEntity.ok("Avatar successfully uploaded!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading avatar.");
        }
    }

    @GetMapping("/profile/get-avatar")
    @ResponseBody
    public byte[] getAvatar(@RequestParam("user_id") long userId) {
        UserAvatar userAvatar = userAvatarRepository.findByUserId(userId);
        return userAvatar != null ? userAvatar.getAvatar() : null;
    }

    @PostMapping("/upload-banner")
    @ResponseBody
    public ResponseEntity<String> handleBannerUpload(@RequestParam("banner") MultipartFile file, @RequestParam("user_id") long userId) {
        try {
            InputStream inputStream = file.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Thumbnails.of(inputStream)
                    .size(1920, 300)
                    .outputQuality(1.0)
                    .keepAspectRatio(true)
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            byte[] imageBytes = outputStream.toByteArray();

            UserBanner userBanner = userBannerRepository.findByUserId(userId);
            if (userBanner == null) {
                userBanner = new UserBanner();
                userBanner.setUserId(userId);
            }

            userBanner.setBanner(imageBytes);
            userBannerRepository.save(userBanner);

            return ResponseEntity.ok("Banner successfully uploaded!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading banner.");
        }
    }

    @GetMapping("/profile/get-banner")
    @ResponseBody
    public byte[] getBanner(@RequestParam("user_id") long userId) {
        UserBanner userBanner = userBannerRepository.findByUserId(userId);
        return userBanner != null ? userBanner.getBanner() : null;
    }

}
