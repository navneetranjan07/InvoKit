package com.invoicetracker.controller;

import com.invoicetracker.dto.request.ChangePasswordRequest;
import com.invoicetracker.dto.request.UpdateProfileRequest;
import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.UserResponse;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Long userId = securityUtils.getCurrentUserId();
        UserResponse user = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        UserResponse user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @DeleteMapping("/account")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        Long userId = securityUtils.getCurrentUserId();
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }
}