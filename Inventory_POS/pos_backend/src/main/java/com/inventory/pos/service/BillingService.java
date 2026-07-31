package com.inventory.pos.service;

import com.inventory.pos.dto.CartItemDto;
import com.inventory.pos.dto.CheckoutRequest;
import com.inventory.pos.dto.ReceiptLine;
import com.inventory.pos.dto.ReceiptResponse;
import com.inventory.pos.model.Product;
import com.inventory.pos.model.Sale;
import com.inventory.pos.model.SaleItem;
import com.inventory.pos.repository.ProductRepository;
import com.inventory.pos.repository.SaleItemRepository;
import com.inventory.pos.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Point-of-sale billing — ported from the original console app's
 * BillingService. A checkout is one DB transaction: it inserts the bill,
 * inserts each line item, and deducts stock atomically (same
 * "stock >= qty" guard as the original raw SQL). If anything fails
 * (e.g. someone else bought the last units first), the whole sale rolls
 * back so stock and bills never go out of sync.
 */
@Service
public class BillingService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private BillingNotificationService billingNotificationService;

    @Autowired
    private BillingEmailService billingEmailService;

    @Autowired
    private CustomerService customerService;

    @org.springframework.beans.factory.annotation.Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Transactional
    public ReceiptResponse checkout(CheckoutRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty.");
        }

        // Merge duplicate product IDs (e.g. same item added twice) the same
        // way the original console app's LinkedHashMap cart did.
        Map<Integer, Integer> mergedQuantities = new LinkedHashMap<>();
        for (CartItemDto item : request.getItems()) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Each cart line needs a valid productId and a quantity > 0.");
            }
            mergedQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        // Look up products and do an upfront friendly stock check
        // (the atomic decrement below is still the real guard).
        Map<Integer, Product> products = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        List<ReceiptLine> lines = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : mergedQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer qty = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            if (product.getStock() == null || product.getStock() < qty) {
                throw new IllegalStateException(
                        "Insufficient stock for " + product.getName() + " (available: "
                                + (product.getStock() == null ? 0 : product.getStock()) + ", requested: " + qty + ")");
            }

            products.put(productId, product);
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(subtotal);
            lines.add(new ReceiptLine(productId, product.getName(), qty, product.getPrice(), subtotal));
        }

        String customerName = (request.getCustomerName() == null || request.getCustomerName().isBlank())
                ? "Walk-in Customer" : request.getCustomerName().trim();
        String customerPhone = (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank())
                ? null : request.getCustomerPhone().trim();
        String customerEmail = (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank())
                ? null : request.getCustomerEmail().trim();

        // Link to the customer master: if a specific customer was picked from the
        // dropdown, use that record directly; otherwise resolve/auto-save by phone
        // so returning customers get recognised (and autofilled) next time.
        Integer customerId = request.getCustomerId();
        if (customerId == null && customerPhone != null) {
            com.inventory.pos.model.Customer customer = customerService.findOrAutoSave(
                    customerName, customerPhone, request.getCustomerEmail(),
                    request.getCustomerGstNumber(), request.getCustomerAddress());
            if (customer != null) {
                customerId = customer.getId();
                if (customerEmail == null && customer.getEmail() != null && !customer.getEmail().isBlank()) {
                    customerEmail = customer.getEmail();
                }
            }
        }
        // If a specific customer was picked from the dropdown but no email was
        // typed on the billing screen, fall back to the email on their record.
        if (customerEmail == null && customerId != null) {
            customerEmail = customerService.getById(customerId)
                    .map(com.inventory.pos.model.Customer::getEmail)
                    .filter(e -> e != null && !e.isBlank())
                    .orElse(null);
        }

        Sale sale = new Sale();
        sale.setBillDate(LocalDateTime.now());
        sale.setTotalAmount(total);
        sale.setCustomerName(customerName);
        sale.setCustomerPhone(customerPhone);
        sale.setCustomerEmail(customerEmail);
        sale.setCustomerId(customerId);
        sale = saleRepository.save(sale);

        for (Map.Entry<Integer, Integer> entry : mergedQuantities.entrySet()) {
            Integer productId = entry.getKey();
            Integer qty = entry.getValue();
            Product product = products.get(productId);

            // Atomic guard: fails (0 rows) if stock changed underneath us
            // since the check above — whole checkout rolls back.
            int updated = productRepository.decrementStockIfAvailable(productId, qty);
            if (updated == 0) {
                throw new IllegalStateException(
                        "Not enough stock left for " + product.getName() + " — sale aborted.");
            }

            SaleItem item = new SaleItem();
            item.setBillId(sale.getBillId());
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setQuantity(qty);
            item.setPrice(product.getPrice());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(qty)));
            saleItemRepository.save(item);
        }

        // Same as the original console app: warn right away if anything
        // just sold dropped to/below its threshold.
        List<Product> nowLowStock = new ArrayList<>();
        for (Integer productId : mergedQuantities.keySet()) {
            productRepository.findById(productId).ifPresent(p -> {
                if (p.isLowStock()) nowLowStock.add(p);
            });
        }
        if (!nowLowStock.isEmpty()) {
            alertService.sendLowStockAlert(nowLowStock);
        }

        ReceiptResponse receipt = new ReceiptResponse(sale.getBillId(), sale.getBillDate(), sale.getCustomerName(),
                sale.getCustomerPhone(), sale.getTotalAmount(), lines);
        receipt.setCustomerId(sale.getCustomerId());
        receipt.setCustomerEmail(sale.getCustomerEmail());
        receipt.setWhatsappShareLink(buildWhatsappShareLink(receipt));

        // Best-effort — a notification hiccup never rolls back or fails the sale itself.
        // Email is the free channel (reuses the shop's existing Gmail SMTP setup).
        receipt.setEmailSent(billingEmailService.emailBill(receipt));
        // SMS/WhatsApp via Twilio, if the shop owner has chosen to pay for it (off by default).
        billingNotificationService.notifyCustomer(receipt);

        return receipt;
    }

    /**
     * Builds a free "click-to-send" wa.me link pre-filled with the bill
     * summary. No WhatsApp Business API, no account, no per-message cost —
     * the cashier just taps the link (or the frontend button backed by it)
     * and WhatsApp opens on their own phone/computer with the message ready
     * to send to the customer's number.
     */
    private String buildWhatsappShareLink(ReceiptResponse receipt) {
        if (receipt.getCustomerPhone() == null || receipt.getCustomerPhone().isBlank()) {
            return null;
        }
        String digits = receipt.getCustomerPhone().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        // Assume Indian numbers if no country code was included (10 digits).
        if (digits.length() == 10) {
            digits = "91" + digits;
        }
        String invoiceLink = backendBaseUrl + "/api/billing/sales/" + receipt.getBillId() + "/invoice";
        String text = String.format(
                "Bill #%d confirmed. Total: Rs. %s%nThank you for shopping with us!%nDownload your invoice: %s",
                receipt.getBillId(), receipt.getTotal().setScale(2, java.math.RoundingMode.HALF_UP), invoiceLink);
        String encoded = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
        return "https://wa.me/" + digits + "?text=" + encoded;
    }

    public List<Sale> getSaleHistory() {
        return saleRepository.findAllByOrderByBillIdDesc();
    }

    public ReceiptResponse getSaleDetail(Integer billId) {
        Sale sale = saleRepository.findById(billId)
                .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + billId));
        List<SaleItem> items = saleItemRepository.findByBillId(billId);

        List<ReceiptLine> lines = new ArrayList<>();
        for (SaleItem item : items) {
            lines.add(new ReceiptLine(item.getProductId(), item.getProductName(),
                    item.getQuantity(), item.getPrice(), item.getSubtotal()));
        }

        ReceiptResponse receipt = new ReceiptResponse(sale.getBillId(), sale.getBillDate(), sale.getCustomerName(),
                sale.getCustomerPhone(), sale.getTotalAmount(), lines);
        receipt.setCustomerId(sale.getCustomerId());
        receipt.setCustomerEmail(sale.getCustomerEmail());
        receipt.setWhatsappShareLink(buildWhatsappShareLink(receipt));
        return receipt;
    }

    /** Re-sends the free invoice email for an existing bill (e.g. "Resend" button). */
    public boolean resendBillEmail(Integer billId) {
        ReceiptResponse receipt = getSaleDetail(billId);
        return billingEmailService.emailBill(receipt);
    }
}

