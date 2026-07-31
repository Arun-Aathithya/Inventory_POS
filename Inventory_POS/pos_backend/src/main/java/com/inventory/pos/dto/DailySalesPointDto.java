package com.inventory.pos.dto;

import java.math.BigDecimal;

public class DailySalesPointDto {
    private String date;
    private BigDecimal revenue;
    private long billCount;

    public DailySalesPointDto() {}

    public DailySalesPointDto(String date, BigDecimal revenue, long billCount) {
        this.date = date;
        this.revenue = revenue;
        this.billCount = billCount;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

    public long getBillCount() { return billCount; }
    public void setBillCount(long billCount) { this.billCount = billCount; }
}
