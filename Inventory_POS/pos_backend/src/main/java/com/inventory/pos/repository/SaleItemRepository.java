package com.inventory.pos.repository;

import com.inventory.pos.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
    List<SaleItem> findByBillId(Integer billId);

    // Best sellers by units sold, for Business Analytics + Product Recommendation fallback.
    @Query(value = "SELECT si.product_id, si.product_name, SUM(si.quantity) AS totalQty, SUM(si.subtotal) AS totalRevenue " +
            "FROM sale_items si GROUP BY si.product_id, si.product_name ORDER BY totalQty DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopSellingProducts(@Param("limit") int limit);

    // Revenue grouped by product category (joins to the Product table).
    @Query(value = "SELECT p.category AS category, SUM(si.subtotal) AS revenue " +
            "FROM sale_items si JOIN Product p ON si.product_id = p.id " +
            "GROUP BY p.category ORDER BY revenue DESC", nativeQuery = true)
    List<Object[]> findRevenueByCategory();

    // Units sold per product since a given date — feeds the AI reorder predictor.
    @Query(value = "SELECT si.product_id, SUM(si.quantity) FROM sale_items si " +
            "JOIN sales s ON si.bill_id = s.bill_id WHERE s.bill_date >= :since GROUP BY si.product_id", nativeQuery = true)
    List<Object[]> getQuantitySoldPerProductSince(@Param("since") LocalDateTime since);

    // "Customers who bought this also bought..." — products that co-occur
    // on the same bill as productId, ranked by how often that happens.
    @Query(value = "SELECT si2.product_id, si2.product_name, COUNT(*) AS freq " +
            "FROM sale_items si1 JOIN sale_items si2 " +
            "  ON si1.bill_id = si2.bill_id AND si1.product_id <> si2.product_id " +
            "WHERE si1.product_id = :productId " +
            "GROUP BY si2.product_id, si2.product_name ORDER BY freq DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findFrequentlyBoughtWith(@Param("productId") Integer productId, @Param("limit") int limit);
}
