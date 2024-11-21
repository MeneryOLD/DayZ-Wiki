package com.dayzwiki.portal.controller;


import com.dayzwiki.portal.model.YoutubeVideo;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.YoutubeVideoRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class YoutubeVideoControllerTest {

    @Mock
    private YoutubeVideoRepository youtubeVideoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private YoutubeVideoController youtubeVideoController;

    @Mock
    private Principal principal;

    private YoutubeVideo sampleVideo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleVideo = new YoutubeVideo();
        sampleVideo.setId("mdQ6k9k9Iig");
        sampleVideo.setName("Sample Video");
        sampleVideo.setActive(true);
        sampleVideo.setOrder(1);
    }

    @Test
    void shouldGetCurrentVideoSuccessfully() {
        when(youtubeVideoRepository.findTopByActiveTrueOrderByOrderAsc()).thenReturn(sampleVideo);

        ResponseEntity<YoutubeVideo> response = youtubeVideoController.getCurrentVideo();

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Sample Video", response.getBody().getName());
    }

    @Test
    void shouldGetVideoDetailsById() {
        when(youtubeVideoRepository.findById("1")).thenReturn(Optional.of(sampleVideo));

        ResponseEntity<YoutubeVideo> response = youtubeVideoController.getVideoDetails("1");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Sample Video", response.getBody().getName());
    }

    @Test
    void shouldReturnEmptyVideoWhenNotFound() {
        when(youtubeVideoRepository.findById("999")).thenReturn(Optional.empty());

        ResponseEntity<YoutubeVideo> response = youtubeVideoController.getVideoDetails("999");

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void shouldCreateVideoSuccessfully() {
        when(principal.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));

        YoutubeVideo newVideo = new YoutubeVideo();
        newVideo.setName("New Video");

        ResponseEntity<?> response = youtubeVideoController.createVideo(newVideo, principal);

        assertEquals(200, response.getStatusCodeValue());
        verify(youtubeVideoRepository, times(1)).save(newVideo);
    }

    @Test
    void shouldUpdateVideoSuccessfully() {
        YoutubeVideo updatedVideo = new YoutubeVideo();
        updatedVideo.setName("Updated Video");
        updatedVideo.setActive(false);
        updatedVideo.setOrder(2);

        when(youtubeVideoRepository.findById("1")).thenReturn(Optional.of(sampleVideo));
        when(youtubeVideoRepository.save(any(YoutubeVideo.class))).thenReturn(updatedVideo);

        ResponseEntity<YoutubeVideo> response = youtubeVideoController.updateVideo("1", updatedVideo);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated Video", response.getBody().getName());
        assertFalse(response.getBody().isActive());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingVideo() {
        YoutubeVideo updatedVideo = new YoutubeVideo();
        updatedVideo.setName("Non-existent Video");

        when(youtubeVideoRepository.findById("999")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                youtubeVideoController.updateVideo("999", updatedVideo)
        );

        assertEquals("Video not found", exception.getMessage());
    }

    @Test
    void shouldDeleteVideoSuccessfully() {
        doNothing().when(youtubeVideoRepository).deleteById("1");

        ResponseEntity<?> response = youtubeVideoController.deleteVideo("1");

        assertEquals(200, response.getStatusCodeValue());
        verify(youtubeVideoRepository, times(1)).deleteById("1");
    }
}
