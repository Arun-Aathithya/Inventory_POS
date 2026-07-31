package com.inventory.pos.dto;

import java.util.List;

// Body of POST /api/billing/checkout
public class CheckoutRequest {
    private List<CartItemDto> items;
    private String customerName;
    private String customerPhone;

    // Optional: if the billing screen's customer dropdown was used to pick an
    // existing customer directly (rather than typing a phone number), this
    // skips the phone-lookup/auto-save step and just links straight to them.
    private Integer customerId;
    private String customerEmail;
    private String customerGstNumber;
    private String customerAddress;

    public CheckoutRequest() {}

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerGstNumber() { return customerGstNumber; }
    public void setCustomerGstNumber(String customerGstNumber) { this.customerGstNumber = customerGstNumber; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
}
