package com.invoicetracker.controller;

import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.TemplateResponse;
import com.invoicetracker.exception.ResourceNotFoundException;
import com.invoicetracker.model.InvoiceTemplate;
import com.invoicetracker.repository.InvoiceTemplateRepository;
import com.invoicetracker.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    @Autowired
    private InvoiceTemplateRepository templateRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getAllTemplates() {
        List<TemplateResponse> templates = templateRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TemplateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable Long id) {
        InvoiceTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
        return ResponseEntity.ok(
                ApiResponse.success(TemplateResponse.fromEntityWithContent(template))
        );
    }

    @GetMapping("/free")
    public ResponseEntity<ApiResponse<List<TemplateResponse>>> getFreeTemplates() {
        List<TemplateResponse> templates = templateRepository.findByIsPremiumFalse()
                .stream()
                .map(TemplateResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
}