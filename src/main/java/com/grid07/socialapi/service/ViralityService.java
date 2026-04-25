package com.grid07.socialapi.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViralityService {

    private final RedisTemplate<String, String> redisTemplate;

    public ViralityService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateScore(Long postId, String interactionType) {
        String key = "post:" + postId + ":virality_score";
        switch (interactionType) {
            case "BOT_REPLY" -> redisTemplate.opsForValue().increment(key, 1);
            case "HUMAN_LIKE" -> redisTemplate.opsForValue().increment(key, 20);
            case "HUMAN_COMMENT" -> redisTemplate.opsForValue().increment(key, 50);
        }
    }

    public Long getScore(Long postId) {
        String raw = redisTemplate.opsForValue().get("post:" + postId + ":virality_score");
        if (raw == null) return 0L;
        return Long.parseLong(raw);
    }
}
