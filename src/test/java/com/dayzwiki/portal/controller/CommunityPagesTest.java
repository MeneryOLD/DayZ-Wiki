package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.demo.DemoData;
import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Renders the community pages against an in-memory database with the demo data. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class CommunityPagesTest {

    @Autowired MockMvc mvc;
    @Autowired DemoData demo;
    @Autowired PostRepository posts;
    @Autowired UserRepository users;
    @Autowired PostImageRepository images;

    private static final RequestPostProcessor KOLYA = user("kolya@example.com").roles("USER");
    private static final RequestPostProcessor ANNA = user("anna@example.com").roles("USER");
    private static final RequestPostProcessor GHOST = user("ghost@example.com").roles("USER", "MODER", "ADMIN");

    @BeforeEach
    void seed() {
        demo.seed();
    }

    private Post findPost(String title) {
        return posts.findAll().stream().filter(p -> p.getTitle().equals(title)).findFirst().orElseThrow();
    }

    @Test
    void postListIsPublicAndShowsOnlyApprovedPosts() throws Exception {
        mvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Found a full M4-A1 at Tisy")))
                .andExpect(content().string(not(containsString("Is the new motorbike worth it?"))))
                .andExpect(content().string(containsString("class=\"post-card\"")));
    }

    @Test
    void approvedPostIsPublic() throws Exception {
        Post p = findPost("Sakhal survival notes");
        mvc.perform(get("/posts/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Keep a fire going")))
                .andExpect(content().string(containsString("post-gallery")))
                .andExpect(content().string(not(containsString("id=\"deleteBtn\""))));
    }

    @Test
    void pendingPostIsHiddenFromOthersButVisibleToAuthorAndModerators() throws Exception {
        Post p = findPost("Is the new motorbike worth it?");
        mvc.perform(get("/posts/" + p.getId())).andExpect(content().string(containsString("off the map")));
        mvc.perform(get("/posts/" + p.getId()).with(ANNA)).andExpect(content().string(not(containsString("motorbike"))));
        mvc.perform(get("/posts/" + p.getId()).with(KOLYA))
                .andExpect(content().string(containsString("Waiting for a moderator")))
                .andExpect(content().string(containsString("id=\"deleteBtn\"")));
        mvc.perform(get("/posts/" + p.getId()).with(GHOST))
                .andExpect(content().string(containsString("data-moderate=\"approve\"")));
    }

    @Test
    void postImagesAreServedWithTheirType() throws Exception {
        Post p = findPost("Found a full M4-A1 at Tisy");
        Integer imageId = images.findIdsByPostId(p.getId()).get(0);
        mvc.perform(get("/posts/images/" + imageId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/webp"));
    }

    @Test
    void profileShowsPostsAndOwnerTools() throws Exception {
        mvc.perform(get("/@Bandit_Kolya"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Base in the woods near Dubky")))
                .andExpect(content().string(not(containsString("Is the new motorbike"))))
                .andExpect(content().string(not(containsString("id=\"editProfileBtn\""))));
        mvc.perform(get("/@Bandit_Kolya").with(KOLYA))
                .andExpect(content().string(containsString("id=\"editProfileBtn\"")))
                .andExpect(content().string(containsString("Is the new motorbike")))
                .andExpect(content().string(containsString("kolya@example.com")));
        mvc.perform(get("/@medic.anna"))
                .andExpect(content().string(containsString("Spam in community posts")));
        mvc.perform(get("/@Ghost").header("Accept-Language", "ru"))
                .andExpect(content().string(containsString("Модератор")));
    }

    @Test
    void moderationPagesRender() throws Exception {
        mvc.perform(get("/moder/posts").with(GHOST))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Looking for a squad")))
                .andExpect(content().string(matchesPattern("(?s).*Waiting for review: \\d+\\. Oldest first\\..*")));
        mvc.perform(get("/moder/youtube/videos").with(GHOST))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("DayZ 1.30 Badlands trailer")))
                .andExpect(content().string(containsString("On the home page")));
        mvc.perform(get("/moder/posts").with(KOLYA)).andExpect(status().isForbidden());
    }

    @Test
    void createEditAndDeleteRespectOwnership() throws Exception {
        MockMultipartFile img = new MockMultipartFile("images", "a.jpg", "image/jpeg",
                new org.springframework.core.io.ClassPathResource("img/test.jpg").getInputStream());
        String body = mvc.perform(multipart("/posts/create").file(img)
                        .param("title", "Night run to the NWAF").param("content", "Bring NVGs and a full canteen.").with(KOLYA))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Integer id = Integer.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
        Post created = posts.findById(id).orElseThrow();
        assertFalse(created.isApproved());
        assertEquals("Bring NVGs and a full canteen.", created.getContent());

        mvc.perform(post("/posts/edit/" + id).param("title", "Hacked title here").param("content", "Not my post at all.").with(ANNA))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/posts/" + id).with(ANNA)).andExpect(status().isForbidden());
        mvc.perform(delete("/posts/" + id).with(KOLYA)).andExpect(status().isOk());
        assertTrue(posts.findById(id).isEmpty());
    }

    @Test
    void longPostTextIsAccepted() throws Exception {
        String text = "Long report. ".repeat(300);
        mvc.perform(multipart("/posts/create").param("title", "A very long report").param("content", text).with(KOLYA))
                .andExpect(status().isOk());
    }

    @Test
    void avatarUploadNeedsAnAccountAndMissingAvatarFallsBack() throws Exception {
        MockMultipartFile img = new MockMultipartFile("avatar", "a.jpg", "image/jpeg",
                new org.springframework.core.io.ClassPathResource("img/test.jpg").getInputStream());
        mvc.perform(multipart("/profile/upload-avatar").file(img)).andExpect(status().isUnauthorized());
        Long annaId = users.findByName("medic.anna").orElseThrow().getId();
        mvc.perform(get("/profile/get-avatar").param("user_id", String.valueOf(annaId)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/img/default-avatar.webp"));
        mvc.perform(multipart("/profile/upload-avatar").file(img).with(ANNA)).andExpect(status().isOk());
        mvc.perform(get("/profile/get-avatar").param("user_id", String.valueOf(annaId)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void homeAndComparePagesRender() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"themeToggle\"")))
                .andExpect(content().string(containsString("id=\"drawerCompare\"")));
        mvc.perform(get("/compare"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"compareOut\"")));
        mvc.perform(get("/range"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"spreadToggle\"")));
    }
}
