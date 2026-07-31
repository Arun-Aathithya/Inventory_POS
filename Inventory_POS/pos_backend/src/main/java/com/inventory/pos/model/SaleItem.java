package com.inventory.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Maps to your existing `sale_items` table (bill line items).
// Kept as plain columns (not a JPA relationship) to mirror the original
// console app's schema usage exactly — no migration needed.
@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_item_id")
    private Integer saleItemId;

    @Column(name = "bill_id")
    private Integer billId;

    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name")
    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal subtotal;

    public SaleItem() {}

    public Integer getSaleItemId() { return saleItemId; }
    public void setSaleItemId(Integer saleItemId) { this.saleItemId = saleItemId; }

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
