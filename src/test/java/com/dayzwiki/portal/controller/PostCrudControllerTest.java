package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.PostDto;
import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostCrudControllerTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostImageRepository postImageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Principal principal;

    private PostCrudController postManagementController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        postManagementController = new PostCrudController(postRepository, postImageRepository, userRepository);
    }

    @Test
    void testCreatePostSuccess() throws Exception {
        Post post = new Post();
        MultipartFile[] images = {new MockMultipartFile("image", "image.jpg", "image/jpeg", "test image content".getBytes())};
        User user = new User();
        user.setEmail("test@example.com");

        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByEmailOrName("testUser", "testUser")).thenReturn(Optional.of(user));

        String response = postManagementController.createPost(post, images, principal);

        assertEquals("redirect:/posts", response);
        assertEquals(user, post.getAuthor());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testCreatePostUserNotFound() {
        Post post = new Post();
        when(principal.getName()).thenReturn("testUser");
        when(userRepository.findByName("testUser")).thenReturn(Optional.empty());

        String response = postManagementController.createPost(post, null, principal);

        assertEquals("redirect:/error", response);
        verify(postRepository, never()).save(post);
    }

    @Test
    void testDeletePost() {
        Integer postId = 1;

        ResponseEntity<Void> response = postManagementController.deletePost(postId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(postRepository, times(1)).deleteById(postId);
        verify(postImageRepository, times(1)).deleteAllByPostId(postId);
    }

    @Test
    void testEditPostSuccess() throws IOException {
        Integer postId = 1;
        PostDto postDto = new PostDto();
        postDto.setTitle("Updated Title");
        postDto.setContent("Updated Content");

        ClassPathResource imgFile = new ClassPathResource("img/icon.webp");
        MultipartFile imageFile = new MockMultipartFile("image", "icon.webp", "image/png", imgFile.getInputStream());

        postDto.setImages(Collections.singletonList(imageFile));

        Post post = new Post();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ResponseEntity<Void> response = postManagementController.editPost(postId, postDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Title", post.getTitle());
        assertEquals("Updated Content", post.getContent());
        verify(postRepository, times(1)).save(post);
        verify(postImageRepository, times(1)).deleteAllByPostId(postId);
    }

    @Test
    void testEditPostNotFound() {
        Integer postId = 1;
        PostDto postDto = new PostDto();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = postManagementController.editPost(postId, postDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(postRepository, never()).save(any());
        verify(postImageRepository, never()).deleteAllByPostId(postId);
    }
}
