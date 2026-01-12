package com.example.ticketing.controller;

import com.example.ticketing.model.Role;
import com.example.ticketing.model.User;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.service.UserService;
//import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("username exists");
        }
        User u = User.builder()
                .username(req.getUsername())
                .password(req.getPassword())
                .fullName(req.getFullName())
                .roles(Set.of(Role.ROLE_USER))
                .build();
        userService.createUser(u);
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            // session is created by Spring Security automatically
            return ResponseEntity.ok("logged-in");
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Principal principal) {
        // logout can be handled client-side by hitting /logout (default) or implement if needed
        return ResponseEntity.ok("logout endpoint - use /logout with POST to invalidate session");
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("not authenticated");
        return ResponseEntity.ok(principal.getName());
    }

    @Data static class RegisterRequest { private String username; private String password; private String fullName; }
    @Data static class LoginRequest { private String username; private String password; }
}
