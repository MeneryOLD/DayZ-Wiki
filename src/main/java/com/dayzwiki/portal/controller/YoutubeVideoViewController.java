package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.model.YoutubeVideo;
import com.dayzwiki.portal.repository.YoutubeVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;


@Controller
@RequestMapping("/moder/youtube")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MODER', 'ADMIN')")
public class YoutubeVideoViewController {
    private final YoutubeVideoRepository videoRepository;

    @GetMapping("/videos")
    public String getAllVideo(Model model) {
        model.addAttribute("videos", videoRepository.findAll().stream()
                .sorted(Comparator.comparing(YoutubeVideo::isActive).reversed()
                        .thenComparingInt(YoutubeVideo::getOrder))
                .toList());
        return "youtube_video_moderation";
    }
}
