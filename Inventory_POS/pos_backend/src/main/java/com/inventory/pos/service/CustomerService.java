package com.inventory.pos.service;

import com.inventory.pos.model.Customer;
import com.inventory.pos.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAll() {
        return customerRepository.findAllByOrderByNameAsc();
    }

    public Optional<Customer> getById(Integer id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> findByPhone(String phone) {
        if (phone == null || phone.isBlank()) return Optional.empty();
        return customerRepository.findByPhone(phone.trim());
    }

    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) return getAll();
        return customerRepository.findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(query.trim(), query.trim());
    }

    public Customer create(Customer customer) {
        if (customer.getPhone() != null && !customer.getPhone().isBlank()
                && customerRepository.findByPhone(customer.getPhone().trim()).isPresent()) {
            throw new IllegalStateException("A customer with phone " + customer.getPhone() + " already exists.");
        }
        return customerRepository.save(customer);
    }

    public Customer update(Integer id, Customer updated) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        existing.setName(updated.getName());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setGstNumber(updated.getGstNumber());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setState(updated.getState());
        return customerRepository.save(existing);
    }

    public void delete(Integer id) {
        customerRepository.deleteById(id);
    }

    // Called from billing: if the phone entered at checkout matches an existing
    // customer, reuse it (and top up any newly-supplied fields like email/GST
    // that weren't on file yet). Otherwise auto-save a new customer record so
    // next time they're recognised — this is the "Auto Save" behaviour from spec,
    // and what powers the autofill-by-phone lookup during future billing.
    public Customer findOrAutoSave(String name, String phone, String email, String gstNumber, String address) {
        if (phone == null || phone.isBlank()) {
            return null; // walk-in customer with no phone — nothing to save/match
        }
        String trimmedPhone = phone.trim();
        Optional<Customer> existing = customerRepository.findByPhone(trimmedPhone);
        if (existing.isPresent()) {
            Customer c = existing.get();
            boolean changed = false;
            if ((c.getEmail() == null || c.getEmail().isBlank()) && email != null && !email.isBlank()) {
                c.setEmail(email); changed = true;
            }
            if ((c.getGstNumber() == null || c.getGstNumber().isBlank()) && gstNumber != null && !gstNumber.isBlank()) {
                c.setGstNumber(gstNumber); changed = true;
            }
            if ((c.getAddress() == null || c.getAddress().isBlank()) && address != null && !address.isBlank()) {
                c.setAddress(address); changed = true;
            }
            return changed ? customerRepository.save(c) : c;
        }
        Customer c = new Customer();
        c.setName((name == null || name.isBlank()) ? "Walk-in Customer" : name.trim());
        c.setPhone(trimmedPhone);
        c.setEmail(email);
        c.setGstNumber(gstNumber);
        c.setAddress(address);
        return customerRepository.save(c);
    }
}
