package com.inventory.pos.dto;

// One line the frontend adds to the cart before checkout.
public class CartItemDto {
    private Integer productId;
    private Integer quantity;

    public CartItemDto() {}

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
