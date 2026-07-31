package com.inventory.pos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

// Maps to your existing `Product` table — column names match Inventory.sql exactly,
// so this works against your current database with no migration needed.
@Entity
@Table(name = "Product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private Integer weight;

    @Column(name = "DOM")
    private LocalDate dom;

    @Column(name = "DOE")
    private LocalDate doe;

    @Column(name = "MRP")
    private BigDecimal mrp;

    private Integer stock;
    private Integer lowstock;

    public Product() {}

    // --- Getters and setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public LocalDate getDom() { return dom; }
    public void setDom(LocalDate dom) { this.dom = dom; }

    public LocalDate getDoe() { return doe; }
    public void setDoe(LocalDate doe) { this.doe = doe; }

    public BigDecimal getMrp() { return mrp; }
    public void setMrp(BigDecimal mrp) { this.mrp = mrp; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getLowstock() { return lowstock; }
    public void setLowstock(Integer lowstock) { this.lowstock = lowstock; }

    // Convenience used by AlertService / frontend to flag low-stock rows
    public boolean isLowStock() {
        return stock != null && lowstock != null && stock <= lowstock;
    }
}
