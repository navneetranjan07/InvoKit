package com.invoicetracker.service;

import com.invoicetracker.model.Invoice;
import com.invoicetracker.model.PaymentReminder;
import com.invoicetracker.model.UserSettings;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.model.enums.ReminderType;
import com.invoicetracker.repository.InvoiceRepository;
import com.invoicetracker.repository.PaymentReminderRepository;
import com.invoicetracker.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentReminderService {

    @Autowired
    private PaymentReminderRepository paymentReminderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private EmailService emailService;

    // ==========================================
    // SCHEDULE REMINDERS FOR INVOICE
    // ==========================================
    @Transactional
    public void scheduleRemindersForInvoice(Invoice invoice) {
        if (invoice.getClient().getEmail() == null) return;

        UserSettings settings = userSettingsRepository
                .findByUserId(invoice.getUser().getId())
                .orElse(null);

        if (settings == null || !Boolean.TRUE.equals(settings.getSendPaymentReminders())) return;

        int daysBefore = settings.getReminderDaysBefore() != null ? settings.getReminderDaysBefore() : 7;
        int daysAfter = settings.getReminderDaysAfter() != null ? settings.getReminderDaysAfter() : 3;

        // Before due reminder
        LocalDate beforeDate = invoice.getDueDate().minusDays(daysBefore);
        if (beforeDate.isAfter(LocalDate.now())) {
            createReminder(invoice, ReminderType.BEFORE_DUE, -daysBefore,
                    beforeDate.atTime(9, 0));
        }

        // On due date reminder
        createReminder(invoice, ReminderType.ON_DUE, 0,
                invoice.getDueDate().atTime(9, 0));

        // After due reminder
        LocalDate afterDate = invoice.getDueDate().plusDays(daysAfter);
        createReminder(invoice, ReminderType.AFTER_DUE, daysAfter,
                afterDate.atTime(9, 0));
    }

    // ==========================================
    // SEND PENDING REMINDERS
    // ==========================================
    @Transactional
    public void sendPendingReminders() {
        List<PaymentReminder> reminders = paymentReminderRepository
                .findRemindersToSend(LocalDateTime.now());

        for (PaymentReminder reminder : reminders) {
            Invoice invoice = reminder.getInvoice();

            // Skip if invoice is already paid or cancelled
            if (invoice.getStatus() == InvoiceStatus.PAID ||
                    invoice.getStatus() == InvoiceStatus.CANCELLED) {
                reminder.markAsSent();
                paymentReminderRepository.save(reminder);
                continue;
            }

            try {
                emailService.sendPaymentReminderEmail(
                        invoice,
                        reminder.getEmailSubject(),
                        reminder.getEmailBody()
                );
                reminder.markAsSent();
                paymentReminderRepository.save(reminder);
            } catch (Exception e) {
                // Log error but continue with other reminders
                System.err.println("Failed to send reminder: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // CREATE REMINDER
    // ==========================================
    private void createReminder(Invoice invoice, ReminderType type,
                                int daysOffset, LocalDateTime scheduledFor) {
        // Check if reminder already exists
        boolean exists = paymentReminderRepository
                .existsByInvoiceIdAndReminderTypeAndIsSentTrue(invoice.getId(), type);
        if (exists) return;

        String subject = buildReminderSubject(invoice, type);
        String body = buildReminderBody(invoice, type);

        PaymentReminder reminder = PaymentReminder.builder()
                .invoice(invoice)
                .reminderType(type)
                .daysOffset(daysOffset)
                .scheduledFor(scheduledFor)
                .emailSubject(subject)
                .emailBody(body)
                .isSent(false)
                .build();

        paymentReminderRepository.save(reminder);
    }

    private String buildReminderSubject(Invoice invoice, ReminderType type) {
        return switch (type) {
            case BEFORE_DUE -> "Payment Due Soon: Invoice #" + invoice.getInvoiceNumber();
            case ON_DUE -> "Payment Due Today: Invoice #" + invoice.getInvoiceNumber();
            case AFTER_DUE -> "Overdue Payment: Invoice #" + invoice.getInvoiceNumber();
        };
    }

    private String buildReminderBody(Invoice invoice, ReminderType type) {
        // Email service handles full body
        return null;
    }
}