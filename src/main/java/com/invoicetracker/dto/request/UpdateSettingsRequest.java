package com.invoicetracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSettingsRequest {

    @Size(max = 3, message = "Currency must be 3 characters")
    private String defaultCurrency;

    @DecimalMin(value = "0.0", message = "Tax rate must be 0 or greater")
    @DecimalMax(value = "100.0", message = "Tax rate must not exceed 100")
    private BigDecimal defaultTaxRate;

    private String defaultPaymentTerms;

    @Size(max = 20, message = "Invoice number prefix must not exceed 20 characters")
    private String invoiceNumberPrefix;

    private Long defaultTemplateId;

    private Boolean sendPaymentReminders;

    private Integer reminderDaysBefore;

    private Integer reminderDaysAfter;
}