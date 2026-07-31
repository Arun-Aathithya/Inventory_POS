package com.inventory.pos.dto;

import java.time.LocalDate;

// One row of the Expiry Prediction Dashboard.
public class ExpiringProductDto {
    private Integer productId;
    private String productName;
    private String category;
    private Integer stock;
    private LocalDate doe;
    private long daysUntilExpiry; // negative if already expired
    private String urgency; // "expired" | "critical" | "warning" | "ok"

    public ExpiringProductDto() {}

    public ExpiringProductDto(Integer productId, String productName, String category, Integer stock,
                               LocalDate doe, long daysUntilExpiry, String urgency) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.stock = stock;
        this.doe = doe;
        this.daysUntilExpiry = daysUntilExpiry;
        this.urgency = urgency;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public LocalDate getDoe() { return doe; }
    public void setDoe(LocalDate doe) { this.doe = doe; }

    public long getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
}
