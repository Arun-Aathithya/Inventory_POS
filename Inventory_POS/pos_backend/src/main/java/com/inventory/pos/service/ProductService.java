package com.inventory.pos.service;

import com.inventory.pos.dto.ExpiringProductDto;
import com.inventory.pos.model.Product;
import com.inventory.pos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AlertService alertService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }

    public Product addProduct(Product product) {
        Product saved = productRepository.save(product);
        checkAndAlertIfLow(saved);
        return saved;
    }

    public Product updateProduct(Integer id, Product updated) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setPrice(updated.getPrice());
        existing.setQuantity(updated.getQuantity());
        existing.setWeight(updated.getWeight());
        existing.setDom(updated.getDom());
        existing.setDoe(updated.getDoe());
        existing.setMrp(updated.getMrp());
        existing.setStock(updated.getStock());
        existing.setLowstock(updated.getLowstock());

        Product saved = productRepository.save(existing);
        checkAndAlertIfLow(saved);
        return saved;
    }

    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public int getLowStockCount() {
        return getLowStockProducts().size();
    }

    // Expiry Prediction Dashboard: products expiring within `withinDays`,
    // colour-coded so the UI can highlight them (expired / critical / warning / ok).
    public List<ExpiringProductDto> getExpiringProducts(int withinDays) {
        LocalDate cutoff = LocalDate.now().plusDays(withinDays);
        List<ExpiringProductDto> result = new ArrayList<>();
        for (Product p : productRepository.findExpiringBy(cutoff)) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), p.getDoe());
            String urgency;
            if (daysUntil < 0) urgency = "expired";
            else if (daysUntil <= 7) urgency = "critical";
            else if (daysUntil <= 30) urgency = "warning";
            else urgency = "ok";

            result.add(new ExpiringProductDto(p.getId(), p.getName(), p.getCategory(), p.getStock(),
                    p.getDoe(), daysUntil, urgency));
        }
        return result;
    }

    // Called after every add/update so an alert fires the moment a product
    // crosses its threshold — not just on the scheduled check.
    private void checkAndAlertIfLow(Product product) {
        if (product.isLowStock()) {
            alertService.sendLowStockAlert(List.of(product));
        }
    }
}
