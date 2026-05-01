package com.invoicetracker.scheduler;

import com.invoicetracker.service.PaymentReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentReminderScheduler {

    @Autowired
    private PaymentReminderService paymentReminderService;

    // Run every hour
    @Scheduled(cron = "0 0 * * * *")
    public void sendPendingReminders() {
        paymentReminderService.sendPendingReminders();
    }
}