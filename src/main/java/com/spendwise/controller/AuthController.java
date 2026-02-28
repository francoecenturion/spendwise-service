package com.spendwise.controller;

import com.spendwise.dto.UserDTO;
import com.spendwise.dto.auth.AuthResponseDTO;
import com.spendwise.dto.auth.LoginRequestDTO;
import com.spendwise.dto.auth.UpdateProfileDTO;
import com.spendwise.service.interfaces.IAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserDTO dto) {
        String message = authService.register(dto);
        log.debug("POST /auth/register finished for {}", dto.getEmail());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestParam String token) {
        String message = authService.verifyEmail(token);
        log.debug("GET /auth/verify finished");
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        AuthResponseDTO response = authService.login(dto);
        log.debug("POST /auth/login finished for {}", dto.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile() {
        return ResponseEntity.ok(authService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody UpdateProfileDTO dto) {
        return ResponseEntity.ok(authService.updateProfile(dto));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount() {
        authService.deleteAccount();
        return ResponseEntity.noContent().build();
    }
}
