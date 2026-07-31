package com.inventory.pos.controller;

import com.inventory.pos.dto.CategoryRevenueDto;
import com.inventory.pos.dto.DailySalesPointDto;
import com.inventory.pos.dto.DashboardStatsDto;
import com.inventory.pos.dto.TopProductDto;
import com.inventory.pos.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Admin Dashboard + Business Analytics endpoints.
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private AnalyticsService analyticsService;

    @Value("${app.expiry.warning-days:30}")
    private int expiryWarningDays;

    @GetMapping("/stats")
    public DashboardStatsDto stats() {
        return analyticsService.getDashboardStats(expiryWarningDays);
    }

    // days=7 for weekly, days=30 for monthly, etc. — the frontend chart picks the range.
    @GetMapping("/sales-trend")
    public List<DailySalesPointDto> salesTrend(@RequestParam(defaultValue = "7") int days) {
        return analyticsService.getDailySalesTrend(days);
    }

    @GetMapping("/category-revenue")
    public List<CategoryRevenueDto> categoryRevenue() {
        return analyticsService.getCategoryRevenue();
    }

    @GetMapping("/top-products")
    public List<TopProductDto> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return analyticsService.getTopSellingProducts(limit);
    }
}
