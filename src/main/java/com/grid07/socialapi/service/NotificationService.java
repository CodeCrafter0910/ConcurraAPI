package com.grid07.socialapi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final Duration NOTIF_COOLDOWN = Duration.ofMinutes(15);

    private final RedisTemplate<String, String> redisTemplate;

    public NotificationService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void handleBotInteraction(Long postOwnerId, String botName) {
        String cooldownKey = "user:" + postOwnerId + ":notif_cooldown";
        String pendingKey = "user:" + postOwnerId + ":pending_notifs";
        String message = "Bot " + botName + " replied to your post";

        Boolean onCooldown = redisTemplate.hasKey(cooldownKey);

        if (Boolean.TRUE.equals(onCooldown)) {
            redisTemplate.opsForList().rightPush(pendingKey, message);
        } else {
            log.info("Push Notification Sent to User: {}", postOwnerId);
            redisTemplate.opsForValue().set(cooldownKey, "1", NOTIF_COOLDOWN);
        }
    }
}
