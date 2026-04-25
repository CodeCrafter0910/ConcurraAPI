package com.grid07.socialapi.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LockService {
    
    private static final int HORIZONTAL_CAP = 100;
    private static final int VERTICAL_CAP = 20;
    private static final Duration COOLDOWN_TTL = Duration.ofMinutes(10);
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public LockService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public boolean tryAcquireBotSlot(Long postId) {
        String key = "post:" + postId + ":bot_count";
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current > HORIZONTAL_CAP) {
            redisTemplate.opsForValue().decrement(key);
            return false;
        }
        return true;
    }
    
    public void releaseBotSlot(Long postId) {
        redisTemplate.opsForValue().decrement("post:" + postId + ":bot_count");
    }
    
    public boolean checkVerticalCap(int depthLevel) {
        return depthLevel <= VERTICAL_CAP;
    }
    
    public boolean tryAcquireCooldown(Long botId, Long humanId) {
        String key = "cooldown:bot_" + botId + ":human_" + humanId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", COOLDOWN_TTL);
        return Boolean.TRUE.equals(acquired);
    }
    
    public Long getBotCount(Long postId) {
        String raw = redisTemplate.opsForValue().get("post:" + postId + ":bot_count");
        return raw == null ? 0L : Long.parseLong(raw);
    }
}
