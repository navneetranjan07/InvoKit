package com.invoicetracker.controller;

import com.invoicetracker.dto.request.CreateInvoiceRequest;
import com.invoicetracker.dto.request.UpdateInvoiceRequest;
import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.InvoiceResponse;
import com.invoicetracker.dto.response.PageResponse;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.createInvoice(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice created successfully", invoice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.updateInvoice(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", invoice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.getInvoice(userId, id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InvoiceResponse>>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) String search) {
        Long userId = securityUtils.getCurrentUserId();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<InvoiceResponse> invoices;

        if (search != null && !search.isEmpty()) {
            invoices = invoiceService.searchInvoices(userId, search, pageable);
        } else if (status != null) {
            invoices = invoiceService.getInvoicesByStatus(userId, status, pageable);
        } else {
            invoices = invoiceService.getAllInvoices(userId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getOverdueInvoices() {
        Long userId = securityUtils.getCurrentUserId();
        List<InvoiceResponse> invoices = invoiceService.getOverdueInvoices(userId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByClient(
            @PathVariable Long clientId) {
        Long userId = securityUtils.getCurrentUserId();
        List<InvoiceResponse> invoices = invoiceService.getInvoicesByClient(userId, clientId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<InvoiceResponse>> sendInvoice(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.sendInvoice(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Invoice sent successfully", invoice));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.markAsPaid(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", invoice));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InvoiceResponse>> cancelInvoice(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.cancelInvoice(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Invoice cancelled", invoice));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ApiResponse<InvoiceResponse>> duplicateInvoice(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        InvoiceResponse invoice = invoiceService.duplicateInvoice(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Invoice duplicated", invoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        invoiceService.deleteInvoice(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully"));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        byte[] pdfBytes = invoiceService.downloadPdf(userId, id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}