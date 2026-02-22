package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.DashboardService;
import com.flow.flowboard.common.dto.dashboard.DashboardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> getDashboardStats() {
        log.info("REST request to get dashboard stats");
        DashboardResponse response = dashboardService.getDashboardStats();
        return ResponseEntity.ok(response);
    }
}
