package com.inventory.pos.controller;

import com.inventory.pos.model.Customer;
import com.inventory.pos.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAll(@RequestParam(required = false) String q) {
        return (q == null || q.isBlank()) ? customerService.getAll() : customerService.search(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getOne(@PathVariable Integer id) {
        return customerService.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Used by the billing screen: as soon as a phone number is entered/selected,
    // the frontend calls this to autofill name/email/GST/address so the user
    // never re-types details for a returning customer.
    @GetMapping("/lookup")
    public ResponseEntity<Customer> lookupByPhone(@RequestParam String phone) {
        return customerService.findByPhone(phone).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Customer customer) {
        try {
            return ResponseEntity.ok(customerService.create(customer));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Integer id, @RequestBody Customer customer) {
        return customerService.update(id, customer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
