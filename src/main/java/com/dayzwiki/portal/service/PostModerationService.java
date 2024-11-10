package com.dayzwiki.portal.service;

import com.dayzwiki.portal.model.Post;
import com.dayzwiki.portal.repository.PostImageRepository;
import com.dayzwiki.portal.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostModerationService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;

    public List<Post> getAllUnapprovedPost() {
        return postRepository.findAllByApproved(false);
    }

    public void approvePost(Integer id) {
        postRepository.findById(id).ifPresent(post -> {
            post.setApproved(true);
            postRepository.save(post);
        });
    }

    public void rejectPost(Integer id) {
        postRepository.findById(id).ifPresent(post -> {
            postImageRepository.deleteAllByPostId(post.getId());
            postRepository.delete(post);
        });
    }

}
