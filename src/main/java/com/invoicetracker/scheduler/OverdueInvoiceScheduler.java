package com.invoicetracker.scheduler;

import com.invoicetracker.model.Invoice;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class OverdueInvoiceScheduler {

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void markOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository
                .findOverdueInvoices(null, LocalDate.now());
        overdueInvoices.forEach(invoice -> {
            if (invoice.getStatus() == InvoiceStatus.SENT) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
            }
        });
    }
}