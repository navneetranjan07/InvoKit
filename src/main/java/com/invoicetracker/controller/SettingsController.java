package com.invoicetracker.controller;

import com.invoicetracker.dto.request.UpdateSettingsRequest;
import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.SettingsResponse;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> getSettings() {
        Long userId = securityUtils.getCurrentUserId();
        SettingsResponse settings = settingsService.getSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<SettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        SettingsResponse settings = settingsService.updateSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Settings updated successfully", settings));
    }
}