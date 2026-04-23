package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.UserSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsResponse {

    private Long id;
    private String defaultCurrency;
    private BigDecimal defaultTaxRate;
    private String defaultPaymentTerms;
    private String invoiceNumberPrefix;
    private Integer nextInvoiceNumber;
    private String nextInvoiceNumberFormatted;
    private Long defaultTemplateId;
    private String defaultTemplateName;
    private Boolean sendPaymentReminders;
    private Integer reminderDaysBefore;
    private Integer reminderDaysAfter;
    private LocalDateTime updatedAt;

    // ========== Static Factory Method ==========

    public static SettingsResponse fromEntity(UserSettings settings) {
        return SettingsResponse.builder()
                .id(settings.getId())
                .defaultCurrency(settings.getDefaultCurrency())
                .defaultTaxRate(settings.getDefaultTaxRate())
                .defaultPaymentTerms(settings.getDefaultPaymentTerms())
                .invoiceNumberPrefix(settings.getInvoiceNumberPrefix())
                .nextInvoiceNumber(settings.getNextInvoiceNumber())
                .nextInvoiceNumberFormatted(settings.getNextInvoiceNumberFormatted())
                .defaultTemplateId(settings.getDefaultTemplate() != null
                        ? settings.getDefaultTemplate().getId() : null)
                .defaultTemplateName(settings.getDefaultTemplate() != null
                        ? settings.getDefaultTemplate().getName() : null)
                .sendPaymentReminders(settings.getSendPaymentReminders())
                .reminderDaysBefore(settings.getReminderDaysBefore())
                .reminderDaysAfter(settings.getReminderDaysAfter())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}