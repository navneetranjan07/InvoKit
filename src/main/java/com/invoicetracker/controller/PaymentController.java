package com.invoicetracker.controller;

import com.invoicetracker.dto.request.RecordPaymentRequest;
import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.PageResponse;
import com.invoicetracker.dto.response.PaymentResponse;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
            @Valid @RequestBody RecordPaymentRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        PaymentResponse payment = paymentService.recordPayment(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully", payment));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "paymentDate"));
        PageResponse<PaymentResponse> payments = paymentService.getAllPayments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {
        Long userId = securityUtils.getCurrentUserId();
        List<PaymentResponse> payments = paymentService.getPaymentsByInvoice(userId, invoiceId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        paymentService.deletePayment(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Payment deleted successfully"));
    }
}