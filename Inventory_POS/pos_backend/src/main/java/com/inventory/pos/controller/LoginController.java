package com.inventory.pos.controller;

import com.inventory.pos.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        boolean success = loginService.authenticate(username, password);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "username", username));
        }
        return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid username or password"));
    }
}
