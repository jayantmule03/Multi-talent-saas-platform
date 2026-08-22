package com.multitalent.analytics.controller;

import com.multitalent.analytics.service.AnalyticsService;
import com.multitalent.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<Map<String, Object>>> today() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getTodayStats()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> byDate(@RequestParam String date) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getStatsForDate(date)));
    }
}
