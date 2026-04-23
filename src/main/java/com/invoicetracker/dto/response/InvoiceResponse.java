package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.Invoice;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.model.enums.RecurringFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceResponse {

    private Long id;
    private String invoiceNumber;
    private String title;
    private InvoiceStatus status;
    private String statusDisplayName;

    // Client info
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientCompanyName;

    // Dates
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDateTime sentAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Financial
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    private String currency;

    // Meta
    private String notes;
    private String terms;
    private String paymentMethod;
    private String pdfUrl;

    // Recurring
    private Boolean isRecurring;
    private RecurringFrequency recurringFrequency;
    private LocalDate nextInvoiceDate;

    // Computed
    private Boolean isOverdue;
    private Boolean isPaid;
    private Boolean isPartiallyPaid;
    private Integer daysOverdue;

    // Items
    private List<InvoiceItemResponse> items;

    // ========== Static Factory Method ==========

    public static InvoiceResponse fromEntity(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .title(invoice.getTitle())
                .status(invoice.getStatus())
                .statusDisplayName(invoice.getStatus().getDisplayName())
                // Client
                .clientId(invoice.getClient().getId())
                .clientName(invoice.getClient().getName())
                .clientEmail(invoice.getClient().getEmail())
                .clientCompanyName(invoice.getClient().getCompanyName())
                // Dates
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .sentAt(invoice.getSentAt())
                .paidAt(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                // Financial
                .subtotal(invoice.getSubtotal())
                .taxRate(invoice.getTaxRate())
                .taxAmount(invoice.getTaxAmount())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .amountPaid(invoice.getAmountPaid())
                .balanceDue(invoice.getBalanceDue())
                .currency(invoice.getCurrency())
                // Meta
                .notes(invoice.getNotes())
                .terms(invoice.getTerms())
                .paymentMethod(invoice.getPaymentMethod())
                .pdfUrl(invoice.getPdfUrl())
                // Recurring
                .isRecurring(invoice.getIsRecurring())
                .recurringFrequency(invoice.getRecurringFrequency())
                .nextInvoiceDate(invoice.getNextInvoiceDate())
                // Computed
                .isOverdue(invoice.isOverdue())
                .isPaid(invoice.isPaid())
                .isPartiallyPaid(invoice.isPartiallyPaid())
                .daysOverdue(invoice.getDaysOverdue())
                // Items
                .items(invoice.getItems().stream()
                        .map(InvoiceItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public static InvoiceResponse fromEntitySimple(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .title(invoice.getTitle())
                .status(invoice.getStatus())
                .statusDisplayName(invoice.getStatus().getDisplayName())
                .clientId(invoice.getClient().getId())
                .clientName(invoice.getClient().getName())
                .clientCompanyName(invoice.getClient().getCompanyName())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .amountPaid(invoice.getAmountPaid())
                .balanceDue(invoice.getBalanceDue())
                .currency(invoice.getCurrency())
                .isOverdue(invoice.isOverdue())
                .isPaid(invoice.isPaid())
                .daysOverdue(invoice.getDaysOverdue())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}