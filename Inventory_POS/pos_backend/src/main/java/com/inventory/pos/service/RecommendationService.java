package com.inventory.pos.service;

import com.inventory.pos.dto.RecommendationDto;
import com.inventory.pos.model.Product;
import com.inventory.pos.repository.ProductRepository;
import com.inventory.pos.repository.SaleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * "Suggest related products during billing based on purchase patterns" —
 * primarily market-basket style co-purchase history (what's frequently
 * bought in the same bill as this product), falling back to same-category
 * products when there isn't enough sales history yet (e.g. a fresh demo DB).
 */
@Service
public class RecommendationService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    public List<RecommendationDto> getRecommendationsForProduct(Integer productId, int limit) {
        List<RecommendationDto> result = new ArrayList<>();
        Set<Integer> alreadyAdded = new LinkedHashSet<>();
        alreadyAdded.add(productId);

        for (Object[] row : saleItemRepository.findFrequentlyBoughtWith(productId, limit)) {
            Integer relatedId = ((Number) row[0]).intValue();
            if (!alreadyAdded.add(relatedId)) continue;
            productRepository.findById(relatedId).ifPresent(p ->
                    result.add(new RecommendationDto(p.getId(), p.getName(), p.getPrice(), p.getStock(), "frequently bought together")));
        }

        if (result.size() < limit) {
            productRepository.findById(productId).ifPresent(source -> {
                for (Product p : productRepository.findAll()) {
                    if (result.size() >= limit) break;
                    if (alreadyAdded.contains(p.getId())) continue;
                    if (p.getCategory() != null && p.getCategory().equalsIgnoreCase(source.getCategory())) {
                        alreadyAdded.add(p.getId());
                        result.add(new RecommendationDto(p.getId(), p.getName(), p.getPrice(), p.getStock(), "same category"));
                    }
                }
            });
        }
        return result;
    }

    // Used for a general "you might also want to stock up on..." panel independent of any one product.
    public List<RecommendationDto> getTopSellersAsRecommendations(int limit) {
        List<RecommendationDto> result = new ArrayList<>();
        for (Object[] row : saleItemRepository.findTopSellingProducts(limit)) {
            Integer productId = ((Number) row[0]).intValue();
            productRepository.findById(productId).ifPresent(p ->
                    result.add(new RecommendationDto(p.getId(), p.getName(), p.getPrice(), p.getStock(), "best seller")));
        }
        return result;
    }
}
