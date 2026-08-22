package com.multitalent.analytics.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Listens to "analytics-events" (published by auth-service and
 * project-service) and increments lightweight daily counters in Redis for
 * real-time dashboards.
 */
@Slf4j
@Component
public class AnalyticsConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    public AnalyticsConsumer(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "analytics-events", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(Object event) {
        String today = LocalDate.now().toString();
        String key = "analytics:events:" + today;
        redisTemplate.opsForHash().increment(key, event.getClass().getSimpleName(), 1);
        log.info("Analytics counter incremented for {} on {}", event.getClass().getSimpleName(), today);
    }
}
