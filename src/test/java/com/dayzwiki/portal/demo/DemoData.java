package com.dayzwiki.portal.demo;

import com.dayzwiki.portal.dto.api.item.UserAvatar;
import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.PostImage;
import com.dayzwiki.portal.model.YoutubeVideo;
import com.dayzwiki.portal.model.user.Ban;
import com.dayzwiki.portal.model.user.Role;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.repository.YoutubeVideoRepository;
import com.dayzwiki.portal.repository.user.RoleRepository;
import com.dayzwiki.portal.repository.user.UserAvatarRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample accounts, posts and videos for the page tests and for local previews (profile "demo").
 * Every demo account uses the password "survivor1".
 */
@Component
@RequiredArgsConstructor
public class DemoData {

    public static final String PASSWORD = "survivor1";

    private final UserRepository users;
    private final RoleRepository roles;
    private final PostRepository posts;
    private final YoutubeVideoRepository videos;
    private final UserAvatarRepository avatars;
    private final PasswordEncoder encoder;
    private final EntityManager em;

    @Transactional
    public void seed() {
        if (users.existsByName("Ghost")) return;
        Role user = role("USER"), moder = role("MODER"), admin = role("ADMIN");

        User ghost = user("Ghost", "ghost@example.com", 420, user, moder, admin);
        User kolya = user("Bandit_Kolya", "kolya@example.com", 96, user);
        User anna = user("medic.anna", "anna@example.com", 12, user);
        avatar(kolya, "img/items/gui-iconbacteria.webp");

        Ban ban = new Ban();
        ban.setUser(anna);
        ban.setBanReason("Spam in community posts");
        ban.setAdminName("Ghost");
        ban.setBanDate(LocalDateTime.now().minusDays(9));
        ban.setUnbanDate(LocalDateTime.now().minusDays(2));
        ban.setActive(false);
        em.persist(ban);

        post(kolya, true, 30, "Found a full M4-A1 at Tisy",
                "Went up to the Tisy military base after a restart. The north barracks had an M4-A1 with a red dot and two STANAG mags.\n\nWatch the hill to the west: someone was camping it with a Mosin.",
                "static/img/maps/chernarus-2.webp", "static/img/maps/chernarus-3.webp");
        post(anna, true, 21, "How I cured cholera with no charcoal",
                "Drank from a pond near Stary Sobor. Vomiting started within ten minutes. No charcoal tabs anywhere, so I ate as much as I could and found tetracycline at the Stary hospital.",
                "static/img/maps/livonia-1.webp");
        post(ghost, true, 14, "Sakhal survival notes",
                "Keep a fire going and wear wool. The volcano area is warm but full of infected. Fish near the coast for steady food.",
                "static/img/maps/sakhal-1.webp", "static/img/maps/sakhal-2.webp", "static/img/maps/sakhal-3.webp");
        post(kolya, true, 6, "Base in the woods near Dubky",
                "Two-gate base with a watchtower. Took three days of hauling planks. Tips: bring a sledgehammer, not a hatchet.");
        post(anna, true, 2, "Nasdara heat is no joke",
                "Heat stroke hits fast in full gear. Drop the plate carrier when you travel and carry two canteens.",
                "static/img/maps/nasdara-1.webp");
        post(kolya, false, 0, "Is the new motorbike worth it?",
                "Tried the 1.30 motorbike on Livonia. Fast, but one crash and you are bleeding. Thoughts?",
                "static/img/maps/livonia-2.webp");
        post(anna, false, 0, "Looking for a squad on EU servers",
                "Medic main, calm voice, evenings CET. Looking for 2-3 people who like slow play.");

        video("dQw4w9WgXcQ", "DayZ 1.30 Badlands trailer", 0, true, 30, ghost);
        video("aqz-KE-bpKQ", "Sakhal survival guide", 1, true, 60, ghost);
        video("jNQXAC9IVRw", "Old Chernarus montage", 5, false, -3, ghost);
    }

    private Role role(String name) {
        return roles.findByName(name).orElseGet(() -> roles.save(new Role(name)));
    }

    private User user(String name, String email, int daysAgo, Role... r) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(PASSWORD));
        u.setEnabled(true);
        u.setCreateDate(LocalDateTime.now().minusDays(daysAgo));
        u.setRoles(new ArrayList<>(List.of(r)));
        return users.save(u);
    }

    private void avatar(User u, String path) {
        UserAvatar a = new UserAvatar();
        a.setUserId(u.getId());
        a.setAvatar(bytes("static/" + path));
        avatars.save(a);
    }

    private void post(User author, boolean approved, int daysAgo, String title, String text, String... images) {
        Post p = new Post();
        p.setAuthor(author);
        p.setTitle(title);
        p.setContent(text);
        p.setApproved(approved);
        p.setCreatedAt(LocalDateTime.now().minusDays(daysAgo).minusHours(3));
        p.setUpdatedAt(p.getCreatedAt());
        for (String path : images) {
            PostImage img = new PostImage();
            img.setPost(p);
            img.setImage(bytes(path));
            p.getPostImages().add(img);
        }
        posts.save(p);
    }

    private void video(String id, String name, int order, boolean active, int expiresInDays, User by) {
        YoutubeVideo v = new YoutubeVideo();
        v.setId(id);
        v.setName(name);
        v.setOrder(order);
        v.setActive(active);
        v.setExpiryDate(LocalDateTime.now().plusDays(expiresInDays));
        v.setUser(by);
        videos.save(v);
    }

    private static byte[] bytes(String path) {
        try {
            return new ClassPathResource(path).getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(path, e);
        }
    }

    /** Fills the database on startup when the "demo" profile is active. */
    @Component
    @Profile("demo")
    @RequiredArgsConstructor
    static class Runner implements ApplicationRunner {
        private final DemoData data;
        private final javax.sql.DataSource dataSource;

        @Override
        public void run(ApplicationArguments args) {
            data.seed();
            // Real item data from db/seed.sql (MySQL-only statements such as SET NAMES are skipped).
            java.io.File seed = new java.io.File("db/seed.sql");
            if (seed.exists()) {
                org.springframework.jdbc.datasource.init.ResourceDatabasePopulator p =
                        new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator(new org.springframework.core.io.FileSystemResource(seed));
                p.setContinueOnError(true);
                p.setSqlScriptEncoding("UTF-8");
                p.execute(dataSource);
            }
        }
    }
}
