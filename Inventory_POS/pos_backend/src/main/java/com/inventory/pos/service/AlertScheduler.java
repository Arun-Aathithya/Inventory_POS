package com.inventory.pos.service;

import com.inventory.pos.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AlertScheduler {

    @Autowired
    private ProductService productService;

    @Autowired
    private AlertService alertService;

    // Runs on a fixed interval set by alerts.schedule.rate-ms (default: every 6 hours).
    // Covers stock changes made outside the app (e.g. direct DB edits) that the
    // per-request check in ProductService wouldn't otherwise catch.
    @Scheduled(fixedRateString = "${alerts.schedule.rate-ms:21600000}")
    public void checkLowStock() {
        List<Product> lowStock = productService.getLowStockProducts();
        if (!lowStock.isEmpty()) {
            alertService.sendLowStockAlert(lowStock);
        }
    }
}
