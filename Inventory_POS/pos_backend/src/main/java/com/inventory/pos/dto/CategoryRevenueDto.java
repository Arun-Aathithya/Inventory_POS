package com.inventory.pos.dto;

import java.math.BigDecimal;

public class CategoryRevenueDto {
    private String category;
    private BigDecimal revenue;

    public CategoryRevenueDto() {}

    public CategoryRevenueDto(String category, BigDecimal revenue) {
        this.category = category;
        this.revenue = revenue;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
}
