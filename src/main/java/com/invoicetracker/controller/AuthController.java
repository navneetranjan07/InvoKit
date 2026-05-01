package com.invoicetracker.controller;

import com.invoicetracker.dto.request.LoginRequest;
import com.invoicetracker.dto.request.RegisterRequest;
import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.AuthResponse;
import com.invoicetracker.dto.response.UserResponse;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.AuthService;
import com.invoicetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Long userId = securityUtils.getCurrentUserId();
        UserResponse user = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}