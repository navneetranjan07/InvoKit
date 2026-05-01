package com.invoicetracker.controller;

import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.DashboardStatsResponse;
import com.invoicetracker.dto.response.RevenueChartResponse;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        Long userId = securityUtils.getCurrentUserId();
        DashboardStatsResponse stats = dashboardService.getDashboardStats(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<ApiResponse<RevenueChartResponse>> getRevenueChart(
            @RequestParam(defaultValue = "6") int months) {
        Long userId = securityUtils.getCurrentUserId();
        RevenueChartResponse chart = dashboardService.getRevenueChart(userId, months);
        return ResponseEntity.ok(ApiResponse.success(chart));
    }
}