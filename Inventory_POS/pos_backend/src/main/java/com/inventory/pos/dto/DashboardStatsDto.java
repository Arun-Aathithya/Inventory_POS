package com.inventory.pos.dto;

import java.math.BigDecimal;
import java.util.List;

// Powers the Admin Dashboard's stat cards + recent activity feed.
public class DashboardStatsDto {
    private long totalProducts;
    private BigDecimal totalStockValue;
    private long todayBillCount;
    private BigDecimal todayRevenue;
    private BigDecimal allTimeRevenue;
    private long totalCustomers;
    private long lowStockCount;
    private long expiringSoonCount;
    private List<RecentActivityDto> recentActivity;

    public DashboardStatsDto() {}

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }

    public long getTodayBillCount() { return todayBillCount; }
    public void setTodayBillCount(long todayBillCount) { this.todayBillCount = todayBillCount; }

    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }

    public BigDecimal getAllTimeRevenue() { return allTimeRevenue; }
    public void setAllTimeRevenue(BigDecimal allTimeRevenue) { this.allTimeRevenue = allTimeRevenue; }

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getExpiringSoonCount() { return expiringSoonCount; }
    public void setExpiringSoonCount(long expiringSoonCount) { this.expiringSoonCount = expiringSoonCount; }

    public List<RecentActivityDto> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivityDto> recentActivity) { this.recentActivity = recentActivity; }
}
