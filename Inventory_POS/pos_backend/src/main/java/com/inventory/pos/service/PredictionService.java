package com.inventory.pos.service;

import com.inventory.pos.dto.ReorderSuggestionDto;
import com.inventory.pos.model.Product;
import com.inventory.pos.repository.ProductRepository;
import com.inventory.pos.repository.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * "AI Sales Prediction" — demand forecasting via a moving average of recent
 * sales velocity (units sold per day, averaged over a lookback window),
 * projected forward to flag products that will run out before a new order
 * could arrive, with a suggested reorder quantity.
 *
 * This is a transparent, explainable heuristic (not a trained ML model) —
 * appropriate for a POS reorder assistant, and easy to demo/explain in a viva.
 */
@Service
public class PredictionService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Value("${app.prediction.lookback-days:30}")
    private int lookbackDays;

    @Value("${app.prediction.lead-time-days:7}")
    private int leadTimeDays;

    // Extra buffer stock (in days of average sales) to order on top of covering the lead time.
    @Value("${app.prediction.safety-buffer-days:3}")
    private int safetyBufferDays;

    public List<ReorderSuggestionDto> getReorderSuggestions() {
        LocalDateTime since = LocalDateTime.now().minusDays(lookbackDays);
        Map<Integer, Double> qtySoldByProduct = new HashMap<>();
        for (Object[] row : saleItemRepository.getQuantitySoldPerProductSince(since)) {
            Integer productId = ((Number) row[0]).intValue();
            double qty = row[1] == null ? 0 : ((Number) row[1]).doubleValue();
            qtySoldByProduct.put(productId, qty);
        }

        List<ReorderSuggestionDto> suggestions = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            double totalSold = qtySoldByProduct.getOrDefault(p.getId(), 0.0);
            double avgDailySales = totalSold / lookbackDays;
            Integer stock = p.getStock() == null ? 0 : p.getStock();

            Double daysLeft = null;
            int suggestedQty = 0;
            String urgency = "ok";

            if (avgDailySales > 0) {
                daysLeft = stock / avgDailySales;
                double targetCoverDays = leadTimeDays + safetyBufferDays;
                if (daysLeft < targetCoverDays) {
                    suggestedQty = (int) Math.ceil(avgDailySales * targetCoverDays - stock);
                    suggestedQty = Math.max(suggestedQty, 0);
                }
                if (daysLeft <= leadTimeDays) {
                    urgency = "critical";
                } else if (daysLeft <= targetCoverDays) {
                    urgency = "warning";
                }
            } else if (p.isLowStock()) {
                // No recent sales history, but already under the manual low-stock
                // threshold — still worth flagging, just without a velocity estimate.
                urgency = "warning";
                suggestedQty = Math.max((p.getLowstock() == null ? 0 : p.getLowstock() * 2) - stock, 0);
            }

            if (!"ok".equals(urgency)) {
                suggestions.add(new ReorderSuggestionDto(
                        p.getId(), p.getName(), stock, round2(avgDailySales), daysLeft == null ? null : round2(daysLeft),
                        suggestedQty, urgency));
            }
        }

        suggestions.sort((a, b) -> {
            int urgencyCompare = urgencyRank(a.getUrgency()) - urgencyRank(b.getUrgency());
            if (urgencyCompare != 0) return urgencyCompare;
            Double da = a.getDaysOfStockLeft() == null ? Double.MAX_VALUE : a.getDaysOfStockLeft();
            Double db = b.getDaysOfStockLeft() == null ? Double.MAX_VALUE : b.getDaysOfStockLeft();
            return da.compareTo(db);
        });
        return suggestions;
    }

    private int urgencyRank(String urgency) {
        return switch (urgency) {
            case "critical" -> 0;
            case "warning" -> 1;
            default -> 2;
        };
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
