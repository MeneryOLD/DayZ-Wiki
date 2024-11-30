package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.YoutubeVideo;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.YoutubeVideoRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/youtube")
@RequiredArgsConstructor
public class YoutubeVideoController {

    private final YoutubeVideoRepository youtubeVideoRepository;
    private final UserRepository userRepository;


    @GetMapping("/video")
    public ResponseEntity<YoutubeVideo> getCurrentVideo() {
        return ResponseEntity.ok(youtubeVideoRepository.findTopByActiveTrueOrderByOrderAsc());
    }

    @GetMapping("/videos/{id}")
    @PreAuthorize("hasAnyRole('MODER', 'ADMIN')")
    public ResponseEntity<YoutubeVideo> getVideoDetails(@PathVariable String id) {
        return ResponseEntity.ok(youtubeVideoRepository.findById(id).orElse(new YoutubeVideo()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MODER', 'ADMIN')")
    public ResponseEntity<?> createVideo(@RequestBody YoutubeVideo video, Principal principal) {
        Optional<User> user = userRepository.findByEmail(principal.getName());
        user.ifPresent(video::setUser);

        youtubeVideoRepository.save(video);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAnyRole('MODER', 'ADMIN')")
    public ResponseEntity<YoutubeVideo> updateVideo(@PathVariable String id, @RequestBody YoutubeVideo updatedVideo) {
        YoutubeVideo resultVideo = youtubeVideoRepository.findById(id)
                .map(video -> {
                    video.setName(updatedVideo.getName());
                    video.setActive(updatedVideo.isActive());
                    video.setOrder(updatedVideo.getOrder());
                    video.setExpiryDate(updatedVideo.getExpiryDate());
                    return youtubeVideoRepository.save(video);
                }).orElseThrow(() -> new RuntimeException("Video not found"));

        return ResponseEntity.ok(resultVideo);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODER', 'ADMIN')")
    public ResponseEntity<?> deleteVideo(@PathVariable String id) {
        youtubeVideoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
