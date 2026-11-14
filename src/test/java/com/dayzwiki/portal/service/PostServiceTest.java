package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.PostImage;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostImageRepository postImageRepository;

    private PostService service;
    private final User author = new User();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PostService(postRepository, postImageRepository);
        author.setId(1L);
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private static MultipartFile jpg() throws IOException {
        return new MockMultipartFile("images", "test.jpg", "image/jpeg", new ClassPathResource("img/test.jpg").getInputStream());
    }

    @Test
    void createsUnapprovedPostWithTrimmedText() throws IOException {
        Post post = service.create(author, "  Tisy loot  ", "Spawned in the north barracks.", List.of(jpg()));

        assertFalse(post.isApproved());
        assertEquals("Tisy loot", post.getTitle());
        assertEquals(author, post.getAuthor());
        assertEquals(1, post.getPostImages().size());
        assertEquals("image/jpeg", PostService.contentType(post.getPostImages().get(0).getImage()));
    }

    @Test
    void rejectsShortTitleAndShortText() {
        assertThrows(PostService.InvalidPostException.class, () -> service.create(author, "Hi", "Long enough text here.", null));
        assertThrows(PostService.InvalidPostException.class, () -> service.create(author, "Fine title", "short", null));
    }

    @Test
    void rejectsMoreThanThreeImages() throws IOException {
        List<MultipartFile> four = List.of(jpg(), jpg(), jpg(), jpg());
        assertThrows(PostService.InvalidPostException.class, () -> service.create(author, "Fine title", "Long enough text here.", four));
    }

    @Test
    void rejectsFilesThatAreNotImages() {
        MultipartFile txt = new MockMultipartFile("images", "a.txt", "text/plain", "definitely not an image".getBytes());
        assertThrows(PostService.InvalidPostException.class, () -> service.create(author, "Fine title", "Long enough text here.", List.of(txt)));
    }

    @Test
    void editKeepsListedImagesAndGoesBackToReview() throws IOException {
        Post post = new Post();
        post.setApproved(true);
        for (int id = 1; id <= 3; id++) {
            PostImage img = new PostImage();
            img.setId(id);
            img.setPost(post);
            post.getPostImages().add(img);
        }

        service.edit(post, "New title", "New text that is long enough.", List.of(2), List.of(jpg()), false);

        assertEquals(2, post.getPostImages().size());
        assertEquals(2, post.getPostImages().get(0).getId());
        assertFalse(post.isApproved());
        assertEquals("New title", post.getTitle());
    }

    @Test
    void moderatorEditKeepsPostApproved() {
        Post post = new Post();
        post.setApproved(true);
        service.edit(post, "New title", "New text that is long enough.", null, null, true);
        assertTrue(post.isApproved());
    }

    @Test
    void accessRules() {
        User stranger = new User();
        stranger.setId(2L);
        Post pending = new Post();
        pending.setAuthor(author);

        assertTrue(PostService.canView(pending, author, false));
        assertFalse(PostService.canView(pending, stranger, false));
        assertFalse(PostService.canView(pending, null, false));
        assertTrue(PostService.canView(pending, stranger, true));
        pending.setApproved(true);
        assertTrue(PostService.canView(pending, null, false));
        assertFalse(PostService.canEdit(pending, stranger, false));
    }
}
