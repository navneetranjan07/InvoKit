package com.invoicetracker.service;

import com.invoicetracker.dto.response.DashboardStatsResponse;
import com.invoicetracker.dto.response.InvoiceResponse;
import com.invoicetracker.dto.response.PaymentResponse;
import com.invoicetracker.dto.response.RevenueChartResponse;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.repository.ClientRepository;
import com.invoicetracker.repository.InvoiceRepository;
import com.invoicetracker.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; //added later

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true) //added later
public class DashboardService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PaymentService paymentService;

    // ==========================================
    // GET DASHBOARD STATS
    // ==========================================
    public DashboardStatsResponse getDashboardStats(Long userId) {
        // Revenue
        BigDecimal totalRevenue = invoiceRepository.calculateTotalRevenue(userId);

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(1)
                .plusMonths(1).atStartOfDay();

        LocalDateTime startOfLastMonth = LocalDate.now().withDayOfMonth(1)
                .minusMonths(1).atStartOfDay();
        LocalDateTime endOfLastMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal revenueThisMonth = invoiceRepository.calculateRevenueForPeriod(
                userId, startOfMonth, endOfMonth);
        BigDecimal revenueLastMonth = invoiceRepository.calculateRevenueForPeriod(
                userId, startOfLastMonth, endOfLastMonth);

        // Growth percentage
        BigDecimal revenueGrowth = BigDecimal.ZERO;
        if (revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = revenueThisMonth.subtract(revenueLastMonth)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(revenueLastMonth, 2, RoundingMode.HALF_UP);
        }

        // Outstanding
        BigDecimal totalOutstanding = invoiceRepository.calculateOutstandingAmount(userId);

        // Overdue amount
        BigDecimal overdueAmount = invoiceRepository
                .findOverdueInvoices(userId, LocalDate.now())
                .stream()
                .map(i -> i.getBalanceDue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Invoice counts
        long totalInvoices = invoiceRepository.countByUserId(userId);
        long draftInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.DRAFT);
        long sentInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.SENT);
        long paidInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.PAID);
        long overdueInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.OVERDUE);
        long cancelledInvoices = invoiceRepository.countByUserIdAndStatus(userId, InvoiceStatus.CANCELLED);

        // Client counts
        long totalClients = clientRepository.countByUserId(userId);
        long activeClients = clientRepository.countByUserIdAndIsActiveTrue(userId);

        // Average invoice value
        BigDecimal avgInvoiceValue = BigDecimal.ZERO;
        if (totalInvoices > 0) {
            avgInvoiceValue = totalRevenue.divide(
                    BigDecimal.valueOf(totalInvoices), 2, RoundingMode.HALF_UP);
        }

        // Payment success rate
        double paymentSuccessRate = 0.0;
        long sentAndPaid = sentInvoices + paidInvoices;
        if (totalInvoices > 0) {
            paymentSuccessRate = (paidInvoices * 100.0) / totalInvoices;
        }

        // Recent data
        List<InvoiceResponse> recentInvoices = invoiceService.getRecentInvoices(userId, 5);
        List<PaymentResponse> recentPayments = paymentService.getRecentPayments(userId, 5);

        return DashboardStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .revenueThisMonth(revenueThisMonth)
                .revenueLastMonth(revenueLastMonth)
                .revenueGrowthPercent(revenueGrowth)
                .totalOutstanding(totalOutstanding)
                .overdueAmount(overdueAmount)
                .totalInvoices(totalInvoices)
                .draftInvoices(draftInvoices)
                .sentInvoices(sentInvoices)
                .paidInvoices(paidInvoices)
                .overdueInvoices(overdueInvoices)
                .cancelledInvoices(cancelledInvoices)
                .totalClients(totalClients)
                .activeClients(activeClients)
                .averageInvoiceValue(avgInvoiceValue)
                .paymentSuccessRate(paymentSuccessRate)
                .recentInvoices(recentInvoices)
                .recentPayments(recentPayments)
                .build();
    }

    // ==========================================
    // GET REVENUE CHART DATA
    // ==========================================
    public RevenueChartResponse getRevenueChart(Long userId, int months) {
        LocalDateTime startDate = LocalDate.now().minusMonths(months).atStartOfDay();
        List<Object[]> rawData = invoiceRepository.getMonthlyRevenue(userId, startDate);

        List<RevenueChartResponse.MonthlyData> monthlyData = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal bestMonthRevenue = BigDecimal.ZERO;
        String bestMonth = "";

        for (Object[] row : rawData) {
            int monthNum = ((Number) row[0]).intValue();
            int year = ((Number) row[1]).intValue();
            BigDecimal revenue = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;

            String monthName = Month.of(monthNum)
                    .getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + year;

            monthlyData.add(RevenueChartResponse.MonthlyData.builder()
                    .month(monthName)
                    .monthNumber(monthNum)
                    .year(year)
                    .revenue(revenue)
                    .build());

            totalRevenue = totalRevenue.add(revenue);

            if (revenue.compareTo(bestMonthRevenue) > 0) {
                bestMonthRevenue = revenue;
                bestMonth = monthName;
            }
        }

        BigDecimal averageMonthly = BigDecimal.ZERO;
        if (!monthlyData.isEmpty()) {
            averageMonthly = totalRevenue.divide(
                    BigDecimal.valueOf(monthlyData.size()), 2, RoundingMode.HALF_UP);
        }

        return RevenueChartResponse.builder()
                .monthlyData(monthlyData)
                .totalRevenue(totalRevenue)
                .averageMonthlyRevenue(averageMonthly)
                .bestMonth(bestMonth)
                .bestMonthRevenue(bestMonthRevenue)
                .build();
    }
}