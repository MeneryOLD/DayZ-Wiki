package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.api.item.UserAvatar;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.user.UserAvatarRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

    private final UserRepository userRepository;
    private final UserAvatarRepository userAvatarRepository;

    @GetMapping("/@{username}")
    public String handleUserRequest(Model model, @PathVariable String username, Principal principal) {
        Optional<User> user = userRepository.findByName(username);
        if (user.isPresent()) {
            UserAvatar userAvatar = userAvatarRepository.findByUserId(user.get().getId());

            boolean isOwner = principal != null && principal.getName().equals(user.get().getEmail());
            model.addAttribute("user", user.get());
            model.addAttribute("isOwner", isOwner);

            String avatarUrl;
            if (userAvatar != null && userAvatar.getAvatar() != null) {
                avatarUrl = "/profile/get-avatar?user_id=" + user.get().getId();
            } else {
                avatarUrl = "https://i.imgur.com/3yY0Kbz.png";
            }
            model.addAttribute("avatarUrl", avatarUrl);

            return "user-profile";
        }
        return "error";
    }

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

}
