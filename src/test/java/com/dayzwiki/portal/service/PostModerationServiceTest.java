package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostModerationServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @InjectMocks
    private PostModerationService postModerationService;

    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        post = new Post();
        post.setId(1);
        post.setApproved(false);
    }

    @Test
    void testGetAllUnapprovedPost() {
        when(postRepository.findAllByApproved(false)).thenReturn(List.of(post));

        List<Post> unapprovedPosts = postModerationService.getAllUnapprovedPost();

        assertNotNull(unapprovedPosts);
        assertEquals(1, unapprovedPosts.size());
        assertEquals(post, unapprovedPosts.get(0));
        verify(postRepository, times(1)).findAllByApproved(false);
    }

    @Test
    void testApprovePostSuccess() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        postModerationService.approvePost(1);

        assertTrue(post.isApproved());
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void testApprovePostNotFound() {

        when(postRepository.findById(1)).thenReturn(Optional.empty());

        postModerationService.approvePost(1);

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void testRejectPostSuccess() {
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        postModerationService.rejectPost(1);

        verify(postRepository, times(1)).delete(post);
        verify(postImageRepository, times(1)).deleteAllByPostId(post.getId());
    }

    @Test
    void testRejectPostNotFound() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        postModerationService.rejectPost(1);

        verify(postRepository, never()).delete(any(Post.class));
        verify(postImageRepository, never()).deleteAllByPostId(anyInt());
    }
}