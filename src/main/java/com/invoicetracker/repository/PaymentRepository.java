package com.invoicetracker.repository;

import com.invoicetracker.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    @Query("SELECT p FROM Payment p WHERE p.invoice.user.id = :userId")
    Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoice.id = :invoiceId")
    BigDecimal calculateTotalPaymentsForInvoice(@Param("invoiceId") Long invoiceId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE " +
           "p.invoice.user.id = :userId AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalPaymentsForPeriod(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT p FROM Payment p WHERE p.invoice.user.id = :userId ORDER BY p.paymentDate DESC")
    List<Payment> findRecentPayments(@Param("userId") Long userId, Pageable pageable);

    long countByInvoiceId(Long invoiceId);

    void deleteByInvoiceId(Long invoiceId);

}