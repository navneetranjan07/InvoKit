package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    // Revenue Stats
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueLastMonth;
    private BigDecimal revenueGrowthPercent;

    // Outstanding Stats
    private BigDecimal totalOutstanding;
    private BigDecimal overdueAmount;

    // Invoice Counts
    private Long totalInvoices;
    private Long draftInvoices;
    private Long sentInvoices;
    private Long paidInvoices;
    private Long overdueInvoices;
    private Long cancelledInvoices;

    // Client Stats
    private Long totalClients;
    private Long activeClients;

    // Recent Data
    private List<InvoiceResponse> recentInvoices;
    private List<PaymentResponse> recentPayments;

    // Quick Metrics
    private BigDecimal averageInvoiceValue;
    private Double paymentSuccessRate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InvoiceCountByStatus {
        private String status;
        private Long count;
        private BigDecimal totalAmount;
    }
}