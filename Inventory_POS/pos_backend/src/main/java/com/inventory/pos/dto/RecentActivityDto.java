package com.inventory.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RecentActivityDto {
    private Integer billId;
    private LocalDateTime billDate;
    private String customerName;
    private BigDecimal totalAmount;

    public RecentActivityDto() {}

    public RecentActivityDto(Integer billId, LocalDateTime billDate, String customerName, BigDecimal totalAmount) {
        this.billId = billId;
        this.billDate = billDate;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
    }

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
