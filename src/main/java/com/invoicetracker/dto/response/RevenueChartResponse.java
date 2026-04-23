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
public class RevenueChartResponse {

    private List<MonthlyData> monthlyData;
    private BigDecimal totalRevenue;
    private BigDecimal averageMonthlyRevenue;
    private String bestMonth;
    private BigDecimal bestMonthRevenue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyData {
        private String month;
        private Integer monthNumber;
        private Integer year;
        private BigDecimal revenue;
        private Long invoiceCount;
    }
}