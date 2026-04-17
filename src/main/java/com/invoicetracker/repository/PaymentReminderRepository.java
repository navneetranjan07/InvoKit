package com.invoicetracker.repository;

import com.invoicetracker.model.PaymentReminder;
import com.invoicetracker.model.enums.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {

    List<PaymentReminder> findByInvoiceId(Long invoiceId);

    List<PaymentReminder> findByIsSentFalse();

    @Query("SELECT r FROM PaymentReminder r WHERE r.isSent = false AND r.scheduledFor <= :now")
    List<PaymentReminder> findRemindersToSend(@Param("now") LocalDateTime now);

    List<PaymentReminder> findByInvoiceIdAndIsSentTrue(Long invoiceId);

    boolean existsByInvoiceIdAndReminderTypeAndIsSentTrue(Long invoiceId, ReminderType reminderType);

    long countByIsSentFalse();

    void deleteByInvoiceId(Long invoiceId);
}