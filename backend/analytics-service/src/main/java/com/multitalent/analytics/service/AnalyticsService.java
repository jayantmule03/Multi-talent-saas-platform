package com.multitalent.analytics.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final RedisTemplate<String, Object> redisTemplate;

    public AnalyticsService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> getTodayStats() {
        String key = "analytics:events:" + LocalDate.now();

        return redisTemplate.opsForHash()
                .entries(key)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> Objects.requireNonNullElse(e.getValue(), 0)
                ));
    }

    public Map<String, Object> getStatsForDate(String date) {
        String key = "analytics:events:" + date;

        return redisTemplate.opsForHash()
                .entries(key)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> Objects.requireNonNullElse(e.getValue(), 0)
                ));
    }
}