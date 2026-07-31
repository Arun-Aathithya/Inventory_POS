package com.inventory.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Response of POST /api/billing/checkout and GET /api/billing/sales/{id}
public class ReceiptResponse {
    private Integer billId;
    private LocalDateTime billDate;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer customerId;
    private BigDecimal total;
    private List<ReceiptLine> items;

    // Free "click-to-send" WhatsApp link (wa.me) pre-filled with the bill
    // summary. Costs nothing and needs no API/account — the cashier just
    // taps it and WhatsApp opens with the message ready to send to the
    // customer's number. Null if the customer has no phone on file.
    private String whatsappShareLink;

    // Whether the automatic free-email invoice was sent successfully.
    private boolean emailSent;

    public ReceiptResponse() {}

    public ReceiptResponse(Integer billId, LocalDateTime billDate, String customerName,
                            String customerPhone, BigDecimal total, List<ReceiptLine> items) {
        this.billId = billId;
        this.billDate = billDate;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.total = total;
        this.items = items;
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getWhatsappShareLink() { return whatsappShareLink; }
    public void setWhatsappShareLink(String whatsappShareLink) { this.whatsappShareLink = whatsappShareLink; }

    public boolean isEmailSent() { return emailSent; }
    public void setEmailSent(boolean emailSent) { this.emailSent = emailSent; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<ReceiptLine> getItems() { return items; }
    public void setItems(List<ReceiptLine> items) { this.items = items; }
}
