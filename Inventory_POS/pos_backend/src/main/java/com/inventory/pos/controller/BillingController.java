package com.inventory.pos.controller;

import com.inventory.pos.dto.CheckoutRequest;
import com.inventory.pos.dto.ReceiptResponse;
import com.inventory.pos.model.Sale;
import com.inventory.pos.service.BillingService;
import com.inventory.pos.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @Autowired
    private PdfService pdfService;

    // Builds a bill from a cart, deducts stock, and returns a receipt —
    // the REST equivalent of the original console app's "New Bill" flow.
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            ReceiptResponse receipt = billingService.checkout(request);
            return ResponseEntity.ok(receipt);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sales")
    public List<Sale> saleHistory() {
        return billingService.getSaleHistory();
    }

    @GetMapping("/sales/{billId}")
    public ResponseEntity<?> saleDetail(@PathVariable Integer billId) {
        try {
            return ResponseEntity.ok(billingService.getSaleDetail(billId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Resends the free invoice email for an existing bill (e.g. a "Resend Email" button).
    @PostMapping("/sales/{billId}/resend-email")
    public ResponseEntity<?> resendEmail(@PathVariable Integer billId) {
        try {
            boolean sent = billingService.resendBillEmail(billId);
            return ResponseEntity.ok(Map.of("emailSent", sent));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Printable PDF invoice — "PDF Invoice and Reports" from the enhancement list.
    // Used by the frontend's "Download Invoice" button and by the SMS/WhatsApp
    // bill-notification link.
    @GetMapping("/sales/{billId}/invoice")
    public ResponseEntity<?> invoice(@PathVariable Integer billId) {
        try {
            ReceiptResponse receipt = billingService.getSaleDetail(billId);
            byte[] pdf = pdfService.generateInvoicePdf(receipt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.inline().filename("invoice-" + billId + ".pdf").build());
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
