package com.inventory.pos.controller;

import com.inventory.pos.dto.ExpiringProductDto;
import com.inventory.pos.dto.RecommendationDto;
import com.inventory.pos.model.Product;
import com.inventory.pos.service.ProductService;
import com.inventory.pos.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Integer id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return productService.addProduct(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Integer id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    public List<Product> lowStock() {
        return productService.getLowStockProducts();
    }

    // Expiry Prediction Dashboard — products expiring within `days` (default 30),
    // colour-coded (expired/critical/warning/ok) for the frontend to highlight.
    @GetMapping("/expiring")
    public List<ExpiringProductDto> expiring(@RequestParam(defaultValue = "30") int days) {
        return productService.getExpiringProducts(days);
    }

    // Product Recommendation — "customers who bought this also bought..." for the billing screen.
    @GetMapping("/{id}/recommendations")
    public List<RecommendationDto> recommendations(@PathVariable Integer id, @RequestParam(defaultValue = "4") int limit) {
        return recommendationService.getRecommendationsForProduct(id, limit);
    }
}
