package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.PostImage;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/** Creating, editing and checking access to community posts. */
@Service
@RequiredArgsConstructor
public class PostService {

    public static final int MAX_TITLE = 120;
    public static final int MAX_CONTENT = 5000;
    public static final int MAX_IMAGES = 3;
    public static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;
    private static final int MAX_IMAGE_SIDE = 1600;

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    /** Thrown for input the person can fix; the message is shown to them. */
    public static class InvalidPostException extends RuntimeException {
        public InvalidPostException(String message) {
            super(message);
        }
    }

    public static boolean canEdit(Post post, User viewer, boolean moderator) {
        return moderator || CurrentUserService.sameUser(post.getAuthor(), viewer);
    }

    public static boolean canView(Post post, User viewer, boolean moderator) {
        return post.isApproved() || canEdit(post, viewer, moderator);
    }

    @Transactional
    public Post create(User author, String title, String content, List<MultipartFile> images) {
        Post post = new Post();
        post.setAuthor(author);
        post.setApproved(false);
        applyText(post, title, content);
        List<MultipartFile> files = nonEmpty(images);
        if (files.size() > MAX_IMAGES) {
            throw new InvalidPostException("You can attach up to 3 images.");
        }
        for (MultipartFile file : files) {
            post.getPostImages().add(toImage(post, file));
        }
        return postRepository.save(post);
    }

    /**
     * Updates the text, keeps the listed existing images, adds new ones.
     * An edited post goes back into the moderation queue unless a moderator edited it.
     */
    @Transactional
    public Post edit(Post post, String title, String content, List<Integer> keepImageIds,
                     List<MultipartFile> newImages, boolean byModerator) {
        applyText(post, title, content);
        Set<Integer> keep = keepImageIds == null ? Set.of() : new HashSet<>(keepImageIds);
        List<MultipartFile> files = nonEmpty(newImages);
        post.getPostImages().removeIf(img -> !keep.contains(img.getId()));
        if (post.getPostImages().size() + files.size() > MAX_IMAGES) {
            throw new InvalidPostException("You can attach up to 3 images.");
        }
        for (MultipartFile file : files) {
            post.getPostImages().add(toImage(post, file));
        }
        if (!byModerator) {
            post.setApproved(false);
        }
        post.updateTimestamps();
        return postRepository.save(post);
    }

    @Transactional
    public void delete(Post post) {
        postImageRepository.deleteAllByPostId(post.getId());
        postRepository.delete(post);
    }

    private static void applyText(Post post, String title, String content) {
        String t = title == null ? "" : title.trim();
        String c = content == null ? "" : content.trim();
        if (t.length() < 3 || t.length() > MAX_TITLE) {
            throw new InvalidPostException("The title must be 3-120 characters.");
        }
        if (c.length() < 10 || c.length() > MAX_CONTENT) {
            throw new InvalidPostException("The text must be 10-5000 characters.");
        }
        post.setTitle(t);
        post.setContent(c);
    }

    private static List<MultipartFile> nonEmpty(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream().filter(f -> f != null && !f.isEmpty()).toList();
    }

    private static PostImage toImage(Post post, MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new InvalidPostException("Each image must be 8 MB or smaller.");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidPostException("Could not read one of the images.");
        }
        PostImage image = new PostImage();
        image.setPost(post);
        image.setImage(shrink(bytes));
        return image;
    }

    /**
     * Scales big photos down to 1600 px and re-encodes them as JPEG (this also drops EXIF data such as GPS).
     * Formats Java cannot decode (WebP, animated GIF) are stored as uploaded if they really are images.
     */
    static byte[] shrink(byte[] bytes) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(new ByteArrayInputStream(bytes))
                    .size(MAX_IMAGE_SIDE, MAX_IMAGE_SIDE)
                    .keepAspectRatio(true)
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .outputFormat("jpg")
                    .outputQuality(0.86)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException | RuntimeException e) {
            if (contentType(bytes) != null) {
                return bytes;
            }
            throw new InvalidPostException("Only JPG, PNG, WebP and GIF images are supported.");
        }
    }

    /** Image MIME type from the file's first bytes, or null if it is not a supported image. */
    public static String contentType(byte[] b) {
        if (b == null || b.length < 12) return null;
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) return "image/jpeg";
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
        if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F') return "image/gif";
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
        return null;
    }

    /** One card in a post list: the post, its first image id (or null) and how many images it has. */
    public record PostCard(Post post, Integer coverId, int imageCount) {}

    public List<PostCard> cards(List<Post> posts) {
        Map<Integer, int[]> covers = covers(posts);
        return posts.stream().map(p -> {
            int[] c = covers.get(p.getId());
            return new PostCard(p, c == null ? null : c[0], c == null ? 0 : c[1]);
        }).toList();
    }

    /** postId -> {first image id, image count} for the given posts. */
    public Map<Integer, int[]> covers(Collection<Post> posts) {
        Map<Integer, int[]> out = new HashMap<>();
        if (posts.isEmpty()) return out;
        List<Integer> ids = posts.stream().map(Post::getId).toList();
        for (Object[] row : postImageRepository.findCovers(ids)) {
            out.put((Integer) row[0], new int[]{(Integer) row[1], ((Number) row[2]).intValue()});
        }
        return out;
    }
}
