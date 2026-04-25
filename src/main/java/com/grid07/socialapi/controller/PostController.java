package com.grid07.socialapi.controller;

import com.grid07.socialapi.dto.CommentRequest;
import com.grid07.socialapi.dto.LikeRequest;
import com.grid07.socialapi.dto.PostRequest;
import com.grid07.socialapi.entity.Comment;
import com.grid07.socialapi.entity.Post;
import com.grid07.socialapi.service.CommentService;
import com.grid07.socialapi.service.PostService;
import com.grid07.socialapi.service.ViralityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final ViralityService viralityService;

    public PostController(PostService postService,
                          CommentService commentService,
                          ViralityService viralityService) {
        this.postService = postService;
        this.commentService = commentService;
        this.viralityService = viralityService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post created = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long postId,
                                              @RequestBody CommentRequest request) {
        Comment saved = commentService.addComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable Long postId,
                                                        @RequestBody LikeRequest request) {
        postService.likePost(postId, request);
        Long score = viralityService.getScore(postId);
        return ResponseEntity.ok(Map.of(
                "message", "Post liked",
                "postId", postId,
                "viralityScore", score
        ));
    }
}
