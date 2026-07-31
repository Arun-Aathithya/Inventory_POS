package com.inventory.pos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// New customer master table. ddl-auto=update will create this automatically
// alongside your existing Product/sales tables — no manual migration needed.
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    // Phone is how we match a walk-in customer entered during billing back
    // to an existing record, so keep it unique.
    @Column(unique = true, length = 20)
    private String phone;

    private String email;

    @Column(name = "gst_number", length = 20)
    private String gstNumber;

    @Column(length = 255)
    private String address;

    private String city;
    private String state;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Customer() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
