package com.grid07.socialapi.service;

import com.grid07.socialapi.dto.LikeRequest;
import com.grid07.socialapi.dto.PostRequest;
import com.grid07.socialapi.entity.Post;
import com.grid07.socialapi.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {
    
    private final PostRepository postRepository;
    private final ViralityService viralityService;
    
    public PostService(PostRepository postRepository, ViralityService viralityService) {
        this.postRepository = postRepository;
        this.viralityService = viralityService;
    }
    
    public Post createPost(PostRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post content cannot be empty");
        }
        if (request.getAuthorType() == null || request.getAuthorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "authorId and authorType are required");
        }
        
        Post post = new Post();
        post.setAuthorId(request.getAuthorId());
        post.setAuthorType(request.getAuthorType().toUpperCase());
        post.setContent(request.getContent());
        
        return postRepository.save(post);
    }
    
    public void likePost(Long postId, LikeRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        
        String type = "USER".equalsIgnoreCase(request.getUserType()) ? "HUMAN_LIKE" : "BOT_REPLY";
        viralityService.updateScore(postId, type);
    }
}
