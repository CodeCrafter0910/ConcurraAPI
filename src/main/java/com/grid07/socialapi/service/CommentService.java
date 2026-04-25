package com.grid07.socialapi.service;

import com.grid07.socialapi.dto.CommentRequest;
import com.grid07.socialapi.entity.Comment;
import com.grid07.socialapi.repository.BotRepository;
import com.grid07.socialapi.repository.CommentRepository;
import com.grid07.socialapi.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final BotRepository botRepository;
    private final LockService lockService;
    private final ViralityService viralityService;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          BotRepository botRepository,
                          LockService lockService,
                          ViralityService viralityService,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.botRepository = botRepository;
        this.lockService = lockService;
        this.viralityService = viralityService;
        this.notificationService = notificationService;
    }

    public Comment addComment(Long postId, CommentRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content cannot be empty");
        }

        if ("BOT".equalsIgnoreCase(request.getAuthorType())) {
            return handleBotComment(postId, request);
        } else {
            return handleHumanComment(postId, request);
        }
    }

    private Comment handleBotComment(Long postId, CommentRequest request) {
        if (!lockService.checkVerticalCap(request.getDepthLevel())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Thread depth limit of 20 exceeded");
        }

        if (!lockService.tryAcquireBotSlot(postId)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Bot reply limit of 100 reached for this post");
        }

        Long postOwnerId = request.getPostOwnerId();
        if (postOwnerId != null) {
            boolean cooldownAcquired = lockService.tryAcquireCooldown(request.getAuthorId(), postOwnerId);
            if (!cooldownAcquired) {
                lockService.releaseBotSlot(postId);
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Bot cooldown active. Please wait 10 minutes before interacting with this user again.");
            }
        }

        Comment comment = buildComment(postId, request, "BOT");

        try {
            Comment saved = commentRepository.save(comment);
            viralityService.updateScore(postId, "BOT_REPLY");

            if (postOwnerId != null) {
                botRepository.findById(request.getAuthorId()).ifPresent(bot ->
                        notificationService.handleBotInteraction(postOwnerId, bot.getName())
                );
            }

            return saved;
        } catch (Exception e) {
            lockService.releaseBotSlot(postId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to persist comment. Redis state rolled back.");
        }
    }

    private Comment handleHumanComment(Long postId, CommentRequest request) {
        Comment comment = buildComment(postId, request, "USER");
        Comment saved = commentRepository.save(comment);
        viralityService.updateScore(postId, "HUMAN_COMMENT");
        return saved;
    }

    private Comment buildComment(Long postId, CommentRequest request, String authorType) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(request.getAuthorId());
        comment.setAuthorType(authorType);
        comment.setContent(request.getContent());
        comment.setDepthLevel(request.getDepthLevel());
        return comment;
    }
}
