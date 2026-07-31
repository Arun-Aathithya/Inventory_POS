package com.inventory.pos.dto;

import java.math.BigDecimal;

public class RecommendationDto {
    private Integer productId;
    private String productName;
    private BigDecimal price;
    private Integer stock;
    private String reason; // "frequently bought together" | "same category"

    public RecommendationDto() {}

    public RecommendationDto(Integer productId, String productName, BigDecimal price, Integer stock, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stock = stock;
        this.reason = reason;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
