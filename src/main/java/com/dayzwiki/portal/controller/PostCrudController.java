package com.dayzwiki.portal.controller;

import com.dayzwiki.portal.dto.PostDto;
import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.model.PostImage;
import com.dayzwiki.portal.model.user.User;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import com.dayzwiki.portal.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/posts")
public class PostCrudController {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public String createPost(@ModelAttribute Post post,
                             @RequestParam("images") MultipartFile[] images,
                             Principal principal) {

        post.setApproved(false);
        Optional<User> user = userRepository.findByName(principal.getName());

        if (user.isPresent()) {
            post.setAuthor(user.get());
        } else {
            return "redirect:/error";
        }

        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        PostImage postImage = new PostImage();
                        postImage.setImage(image.getBytes());
                        postImage.setPost(post);
                        post.getPostImages().add(postImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        postRepository.save(post);
        return "redirect:/posts";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        postRepository.deleteById(id);
        postImageRepository.deleteAllByPostId(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<Void> editPost(@PathVariable Integer id, @ModelAttribute PostDto postDto) {

        Optional<Post> postOptional = postRepository.findById(id);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();

            post.setApproved(false);
            post.setTitle(postDto.getTitle());
            post.setContent(postDto.getContent());
            post.updateTimestamps();

            postImageRepository.deleteAllByPostId(id);

            for (MultipartFile file : postDto.getImages()) {
                try {
                    PostImage postImage = new PostImage();
                    postImage.setImage(file.getBytes());
                    postImage.setPost(post);
                    post.getPostImages().add(postImage);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            postRepository.save(post);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}