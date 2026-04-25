package com.grid07.socialapi.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class NotificationSweeper {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationSweeper.class);
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public NotificationSweeper(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Scheduled(fixedRate = 300000)
    public void sweep() {
        Set<String> pendingKeys = redisTemplate.keys("user:*:pending_notifs");
        
        if (pendingKeys == null || pendingKeys.isEmpty()) {
            return;
        }
        
        for (String key : pendingKeys) {
            List<String> messages = redisTemplate.opsForList().range(key, 0, -1);
            
            if (messages == null || messages.isEmpty()) {
                continue;
            }
            
            String userId = key.split(":")[1];
            int total = messages.size();
            
            String firstMessage = messages.get(0);
            String botName = firstMessage.replace(" replied to your post", "").replace("Bot ", "");
            
            if (total == 1) {
                log.info("Summarized Push Notification for user {}: {} replied to your post.", userId, botName);
            } else {
                log.info("Summarized Push Notification for user {}: {} and {} others interacted with your posts.",
                        userId, botName, total - 1);
            }
            
            redisTemplate.delete(key);
        }
    }
}
