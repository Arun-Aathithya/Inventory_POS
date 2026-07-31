package com.inventory.pos.repository;

import com.inventory.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Integer> {
    List<Sale> findAllByOrderByBillIdDesc();

    // Most recent N bills, for the dashboard's "recent activity" feed.
    List<Sale> findTop5ByOrderByBillIdDesc();

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE DATE(bill_date) = CURDATE()", nativeQuery = true)
    BigDecimal getTodayRevenue();

    @Query(value = "SELECT COUNT(*) FROM sales WHERE DATE(bill_date) = CURDATE()", nativeQuery = true)
    long getTodayBillCount();

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM sales", nativeQuery = true)
    BigDecimal getAllTimeRevenue();

    @Query(value = "SELECT COUNT(DISTINCT customer_phone) FROM sales WHERE customer_phone IS NOT NULL AND customer_phone <> ''", nativeQuery = true)
    long countDistinctCustomers();

    // Day-by-day revenue + bill count since a given date, for the sales trend chart.
    @Query(value = "SELECT DATE(bill_date) AS saleDate, SUM(total_amount) AS revenue, COUNT(*) AS billCount " +
            "FROM sales WHERE bill_date >= :since GROUP BY DATE(bill_date) ORDER BY saleDate ASC", nativeQuery = true)
    List<Object[]> getDailySalesSince(@Param("since") LocalDateTime since);
}
