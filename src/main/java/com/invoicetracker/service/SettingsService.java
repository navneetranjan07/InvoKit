package com.invoicetracker.service;

import com.invoicetracker.dto.request.UpdateSettingsRequest;
import com.invoicetracker.dto.response.SettingsResponse;
import com.invoicetracker.exception.ResourceNotFoundException;
import com.invoicetracker.model.InvoiceTemplate;
import com.invoicetracker.model.UserSettings;
import com.invoicetracker.model.Userr;
import com.invoicetracker.repository.InvoiceTemplateRepository;
import com.invoicetracker.repository.UserRepository;
import com.invoicetracker.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceTemplateRepository templateRepository;

    // ==========================================
    // GET SETTINGS
    // ==========================================
    public SettingsResponse getSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return SettingsResponse.fromEntity(settings);
    }

    // ==========================================
    // UPDATE SETTINGS
    // ==========================================
    @Transactional
    public SettingsResponse updateSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (request.getDefaultCurrency() != null) {
            settings.setDefaultCurrency(request.getDefaultCurrency());
        }
        if (request.getDefaultTaxRate() != null) {
            settings.setDefaultTaxRate(request.getDefaultTaxRate());
        }
        if (request.getDefaultPaymentTerms() != null) {
            settings.setDefaultPaymentTerms(request.getDefaultPaymentTerms());
        }
        if (request.getInvoiceNumberPrefix() != null) {
            settings.setInvoiceNumberPrefix(request.getInvoiceNumberPrefix());
        }
        if (request.getSendPaymentReminders() != null) {
            settings.setSendPaymentReminders(request.getSendPaymentReminders());
        }
        if (request.getReminderDaysBefore() != null) {
            settings.setReminderDaysBefore(request.getReminderDaysBefore());
        }
        if (request.getReminderDaysAfter() != null) {
            settings.setReminderDaysAfter(request.getReminderDaysAfter());
        }
        if (request.getDefaultTemplateId() != null) {
            InvoiceTemplate template = templateRepository.findById(request.getDefaultTemplateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Template", request.getDefaultTemplateId()));
            settings.setDefaultTemplate(template);
        }

        settings = userSettingsRepository.save(settings);
        return SettingsResponse.fromEntity(settings);
    }

    // ==========================================
    // CREATE DEFAULT SETTINGS
    // ==========================================
    private UserSettings createDefaultSettings(Long userId) {
        Userr user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        UserSettings settings = UserSettings.builder()
                .user(user)
                .build();
        return userSettingsRepository.save(settings);
    }
}