package com.visa.cardapi.controller;
import com.visa.cardapi.model.User;
import com.visa.cardapi.repository.UserRepository;
import com.visa.cardapi.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtUtil jwt;
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    public AuthController(JwtUtil j, UserRepository repo, PasswordEncoder passwordEncoder) {
        this.jwt = j;
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }
    public static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = repo.findByUsername(req.username)
                .orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not found");
        }
        if (!passwordEncoder.matches(req.password.trim(), user.getPassword().trim())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }
        String token = jwt.generate(user.getUsername());
        return ResponseEntity.ok(Map.of("token", token));
    }
}