package com.inventory.pos.service;

import com.inventory.pos.model.User;
import com.inventory.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserRepository userRepository;

    public boolean authenticate(String username, String plainPassword) {
        String hashed = PasswordUtil.hash(plainPassword);
        return userRepository.findByUsername(username)
                .map(u -> u.getPassword().equals(hashed))
                .orElse(false);
    }
}
