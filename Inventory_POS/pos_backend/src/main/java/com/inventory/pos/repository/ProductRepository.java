package com.inventory.pos.repository;

import com.inventory.pos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Products at or below their configured lowstock threshold
    @Query("SELECT p FROM Product p WHERE p.stock <= p.lowstock")
    List<Product> findLowStockProducts();

    // Products whose expiry date falls between today and the given cutoff
    // (used by the Expiry Prediction Dashboard). Includes already-expired
    // items too (doe < today) so they show up as "expired", not silently drop off.
    @Query("SELECT p FROM Product p WHERE p.doe IS NOT NULL AND p.doe <= :cutoff ORDER BY p.doe ASC")
    List<Product> findExpiringBy(@Param("cutoff") LocalDate cutoff);

    @Query("SELECT COALESCE(SUM(p.price * p.stock), 0) FROM Product p")
    java.math.BigDecimal getTotalStockValue();

    // Atomic "deduct if enough stock" — same guard as the original console
    // BillingService's raw SQL (`stock = stock - ? WHERE stock >= ?`), so a
    // sale can never push stock negative even under concurrent checkouts.
    // Returns the number of rows updated: 0 means not enough stock was left.
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty WHERE p.id = :id AND p.stock >= :qty")
    int decrementStockIfAvailable(@Param("id") Integer id, @Param("qty") Integer qty);
}
