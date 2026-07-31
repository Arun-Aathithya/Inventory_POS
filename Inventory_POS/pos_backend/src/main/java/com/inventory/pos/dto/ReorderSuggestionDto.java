package com.inventory.pos.dto;

// One row of the AI Sales Prediction / reorder recommendation panel.
public class ReorderSuggestionDto {
    private Integer productId;
    private String productName;
    private Integer currentStock;
    private double avgDailySales;
    private Double daysOfStockLeft; // null if there's no recent sales velocity to estimate from
    private int suggestedReorderQty;
    private String urgency; // "critical" | "warning" | "ok"

    public ReorderSuggestionDto() {}

    public ReorderSuggestionDto(Integer productId, String productName, Integer currentStock,
                                 double avgDailySales, Double daysOfStockLeft,
                                 int suggestedReorderQty, String urgency) {
        this.productId = productId;
        this.productName = productName;
        this.currentStock = currentStock;
        this.avgDailySales = avgDailySales;
        this.daysOfStockLeft = daysOfStockLeft;
        this.suggestedReorderQty = suggestedReorderQty;
        this.urgency = urgency;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public double getAvgDailySales() { return avgDailySales; }
    public void setAvgDailySales(double avgDailySales) { this.avgDailySales = avgDailySales; }

    public Double getDaysOfStockLeft() { return daysOfStockLeft; }
    public void setDaysOfStockLeft(Double daysOfStockLeft) { this.daysOfStockLeft = daysOfStockLeft; }

    public int getSuggestedReorderQty() { return suggestedReorderQty; }
    public void setSuggestedReorderQty(int suggestedReorderQty) { this.suggestedReorderQty = suggestedReorderQty; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
}
