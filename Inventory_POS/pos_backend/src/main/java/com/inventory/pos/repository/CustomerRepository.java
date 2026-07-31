package com.inventory.pos.repository;

import com.inventory.pos.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // Exact-match lookup used by billing's "customer exists -> autofill" flow.
    Optional<Customer> findByPhone(String phone);

    // Autosuggest / search box — matches on name or phone.
    List<Customer> findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(String name, String phone);

    List<Customer> findAllByOrderByNameAsc();
}
