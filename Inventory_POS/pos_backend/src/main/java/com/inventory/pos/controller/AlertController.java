package com.inventory.pos.controller;

import com.inventory.pos.model.Product;
import com.inventory.pos.service.AlertService;
import com.inventory.pos.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AlertService alertService;

    // Lets the frontend show an "Alerts" panel and a "Send now" test button,
    // instead of only relying on the scheduled background job.
    // IMPORTANT: reports the ACTUAL delivery outcome (including SMTP/Twilio failures),
    // not just "we attempted it" — a previous version claimed success even when the
    // send silently failed due to unconfigured credentials.
    @PostMapping("/check-now")
    public Map<String, Object> checkNow() {
        List<Product> lowStock = productService.getLowStockProducts();
        if (lowStock.isEmpty()) {
            return Map.of("lowStockCount", 0, "emailSent", false, "smsSent", false);
        }
        AlertService.AlertResult result = alertService.sendLowStockAlert(lowStock);
        return Map.of(
                "lowStockCount", lowStock.size(),
                "emailSent", result.emailSucceeded(),
                "emailError", result.emailError() == null ? "" : result.emailError(),
                "smsSent", result.smsSucceeded(),
                "smsError", result.smsError() == null ? "" : result.smsError()
        );
    }

    // Standalone SMTP sanity check — sends a real test email independent of any
    // low-stock data, so you can verify Gmail App Password / SMTP config directly.
    @PostMapping("/test-email")
    public Map<String, Object> testEmail() {
        try {
            alertService.sendTestEmail();
            return Map.of("success", true, "message", "Test email sent successfully.");
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            return Map.of("success", false, "message", cause.getMessage() != null ? cause.getMessage() : e.toString());
        }
    }
}
