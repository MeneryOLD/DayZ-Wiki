package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.PostDto;
import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.service.CurrentUserService;
import com.dayzwiki.portal.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostCrudControllerTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostService postService;
    @Mock
    private CurrentUserService currentUser;

    private PostCrudController controller;
    private User author;
    private User stranger;
    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PostCrudController(postRepository, postService, currentUser);
        author = user(1L, "author@example.com");
        stranger = user(2L, "other@example.com");
        post = new Post();
        post.setId(10);
        post.setAuthor(author);
        when(postRepository.findById(10)).thenReturn(Optional.of(post));
    }

    private static User user(long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    private Principal signedIn(User u, String... roles) {
        Principal p = new UsernamePasswordAuthenticationToken(u.getEmail(), null,
                java.util.Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
        when(currentUser.find(p)).thenReturn(Optional.of(u));
        return p;
    }

    private static PostDto dto() {
        PostDto dto = new PostDto();
        dto.setTitle("Found an M4 at Tisy");
        dto.setContent("Spawned in the north barracks after a server restart.");
        return dto;
    }

    @Test
    void createRequiresSignIn() {
        ResponseEntity<?> r = controller.createPost(dto(), null);
        assertEquals(HttpStatus.UNAUTHORIZED, r.getStatusCode());
        verifyNoInteractions(postService);
    }

    @Test
    void createSavesForSignedInUser() {
        Post saved = new Post();
        saved.setId(11);
        when(postService.create(eq(author), anyString(), anyString(), any())).thenReturn(saved);

        ResponseEntity<?> r = controller.createPost(dto(), signedIn(author));

        assertEquals(HttpStatus.OK, r.getStatusCode());
        verify(postService).create(eq(author), eq("Found an M4 at Tisy"), anyString(), any());
    }

    @Test
    void createRejectsInvalidInput() {
        when(postService.create(any(), any(), any(), any())).thenThrow(new PostService.InvalidPostException("The title must be 3-120 characters."));

        ResponseEntity<?> r = controller.createPost(dto(), signedIn(author));

        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void strangerCannotEdit() {
        ResponseEntity<?> r = controller.editPost(10, dto(), signedIn(stranger));
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        verify(postService, never()).edit(any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void authorCanEdit() {
        when(postService.edit(eq(post), any(), any(), any(), any(), eq(false))).thenReturn(post);
        ResponseEntity<?> r = controller.editPost(10, dto(), signedIn(author));
        assertEquals(HttpStatus.OK, r.getStatusCode());
    }

    @Test
    void editMissingPostIs404() {
        ResponseEntity<?> r = controller.editPost(99, dto(), signedIn(author));
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
    }

    @Test
    void strangerCannotDelete() {
        ResponseEntity<?> r = controller.deletePost(10, signedIn(stranger));
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        verify(postService, never()).delete(any());
    }

    @Test
    void anonymousCannotDelete() {
        ResponseEntity<?> r = controller.deletePost(10, null);
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        verify(postService, never()).delete(any());
    }

    @Test
    void authorCanDelete() {
        ResponseEntity<?> r = controller.deletePost(10, signedIn(author));
        assertEquals(HttpStatus.OK, r.getStatusCode());
        verify(postService).delete(post);
    }

    @Test
    void moderatorCanDeleteAnyPost() {
        ResponseEntity<?> r = controller.deletePost(10, signedIn(stranger, "MODER"));
        assertEquals(HttpStatus.OK, r.getStatusCode());
        verify(postService).delete(post);
    }

    @Test
    void moderatorEditKeepsApproval() {
        when(postService.edit(eq(post), any(), any(), any(), any(), eq(true))).thenReturn(post);
        ResponseEntity<?> r = controller.editPost(10, dto(), signedIn(stranger, "ADMIN"));
        assertEquals(HttpStatus.OK, r.getStatusCode());
        verify(postService).edit(eq(post), any(), any(), eq((List<Integer>) null), any(), eq(true));
    }
}
