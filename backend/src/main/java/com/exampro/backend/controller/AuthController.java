
package com.exampro.backend.controller;

import java.util.Base64;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> data) {

        String email = data.get("email");
        String password = data.get("password");

        try {
            Map<String, Object> user = jdbcTemplate.queryForMap(
                "SELECT user_id, password_hash FROM Users WHERE email = ?",
                email
            );

            String storedHash = (String) user.get("password_hash");
            String[] parts = storedHash.split(":");

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 120000, 256
            );

            SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] actualHash =
                factory.generateSecret(spec).getEncoded();

            spec.clearPassword();

            boolean valid = java.security.MessageDigest.isEqual(
                expectedHash, actualHash
            );

            if (valid) {
                return Map.of(
                    "success", true,
                    "message", "Login successful",
                    "studentId", user.get("user_id")
                );
            }

            return Map.of(
                "success", false,
                "message", "Invalid email or password"
            );

        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Invalid email or password"
            );
        }
    }
}
