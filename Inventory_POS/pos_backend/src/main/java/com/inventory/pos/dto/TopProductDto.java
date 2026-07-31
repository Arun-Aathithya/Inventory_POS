package com.inventory.pos.dto;

import java.math.BigDecimal;

public class TopProductDto {
    private Integer productId;
    private String productName;
    private long quantitySold;
    private BigDecimal revenue;

    public TopProductDto() {}

    public TopProductDto(Integer productId, String productName, long quantitySold, BigDecimal revenue) {
        this.productId = productId;
        this.productName = productName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public long getQuantitySold() { return quantitySold; }
    public void setQuantitySold(long quantitySold) { this.quantitySold = quantitySold; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
