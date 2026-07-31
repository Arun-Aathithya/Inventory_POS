package com.inventory.pos.service;

import com.inventory.pos.dto.*;
import com.inventory.pos.model.Sale;
import com.inventory.pos.repository.ProductRepository;
import com.inventory.pos.repository.SaleItemRepository;
import com.inventory.pos.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Backs the Admin Dashboard (stat cards + recent activity) and the
 * Business Analytics screen (daily/weekly/monthly sales trend,
 * category-wise revenue, top-selling products).
 */
@Service
public class AnalyticsService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    public DashboardStatsDto getDashboardStats(int expiryWarningDays) {
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalProducts(productRepository.count());
        dto.setTotalStockValue(nvl(productRepository.getTotalStockValue()));
        dto.setTodayBillCount(saleRepository.getTodayBillCount());
        dto.setTodayRevenue(nvl(saleRepository.getTodayRevenue()));
        dto.setAllTimeRevenue(nvl(saleRepository.getAllTimeRevenue()));
        dto.setTotalCustomers(saleRepository.countDistinctCustomers());
        dto.setLowStockCount(productRepository.findLowStockProducts().size());
        dto.setExpiringSoonCount(productRepository.findExpiringBy(LocalDate.now().plusDays(expiryWarningDays)).size());

        List<RecentActivityDto> recent = new ArrayList<>();
        for (Sale s : saleRepository.findTop5ByOrderByBillIdDesc()) {
            recent.add(new RecentActivityDto(s.getBillId(), s.getBillDate(), s.getCustomerName(), s.getTotalAmount()));
        }
        dto.setRecentActivity(recent);
        return dto;
    }

    // days=7 -> weekly trend, days=30 -> monthly trend, days=1 -> today only, etc.
    public List<DailySalesPointDto> getDailySalesTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days - 1L).toLocalDate().atStartOfDay();
        List<Object[]> rows = saleRepository.getDailySalesSince(since);
        List<DailySalesPointDto> points = new ArrayList<>();
        for (Object[] row : rows) {
            String date = row[0] instanceof Date ? row[0].toString() : String.valueOf(row[0]);
            BigDecimal revenue = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
            long billCount = ((Number) row[2]).longValue();
            points.add(new DailySalesPointDto(date, revenue, billCount));
        }
        return points;
    }

    public List<CategoryRevenueDto> getCategoryRevenue() {
        List<Object[]> rows = saleItemRepository.findRevenueByCategory();
        List<CategoryRevenueDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String category = row[0] == null ? "Uncategorized" : row[0].toString();
            BigDecimal revenue = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
            result.add(new CategoryRevenueDto(category, revenue));
        }
        return result;
    }

    public List<TopProductDto> getTopSellingProducts(int limit) {
        List<Object[]> rows = saleItemRepository.findTopSellingProducts(limit);
        List<TopProductDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            Integer productId = ((Number) row[0]).intValue();
            String name = (String) row[1];
            long qty = ((Number) row[2]).longValue();
            BigDecimal revenue = row[3] == null ? BigDecimal.ZERO : new BigDecimal(row[3].toString());
            result.add(new TopProductDto(productId, name, qty, revenue));
        }
        return result;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
