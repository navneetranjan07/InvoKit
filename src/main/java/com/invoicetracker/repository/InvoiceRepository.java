package com.invoicetracker.repository;

import com.invoicetracker.model.Invoice;
import com.invoicetracker.model.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    Optional<Invoice> findByIdAndUserId(Long id, Long userId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status);

    Page<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status, Pageable pageable);

    List<Invoice> findByClientId(Long clientId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, InvoiceStatus status);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.user.id = :userId AND i.createdAt >= :startDate")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId,
                                        @Param("startDate") LocalDateTime startDate);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND " +
           "i.status IN ('SENT', 'OVERDUE') AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("userId") Long userId,
                                      @Param("today") LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND " +
           "i.status IN ('SENT', 'OVERDUE') AND i.dueDate BETWEEN :today AND :futureDate")
    List<Invoice> findInvoicesDueSoon(@Param("userId") Long userId,
                                      @Param("today") LocalDate today,
                                      @Param("futureDate") LocalDate futureDate);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE " +
           "i.user.id = :userId AND i.status = 'PAID'")
    BigDecimal calculateTotalRevenue(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE " +
           "i.user.id = :userId AND i.status = 'PAID' AND " +
           "i.paidAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateRevenueForPeriod(@Param("userId") Long userId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(i.totalAmount - i.amountPaid), 0) FROM Invoice i WHERE " +
           "i.user.id = :userId AND i.status IN ('SENT', 'OVERDUE')")
    BigDecimal calculateOutstandingAmount(@Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId ORDER BY i.createdAt DESC")
    List<Invoice> findRecentInvoices(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.isRecurring = true AND " +
           "i.nextInvoiceDate <= :today AND i.status != 'CANCELLED'")
    List<Invoice> findRecurringInvoicesDue(@Param("today") LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(i.client.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Invoice> searchInvoices(@Param("userId") Long userId,
                                 @Param("search") String search,
                                 Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.user.id = :userId AND " +
           "i.issueDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByDateRange(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT i.status, COUNT(i), COALESCE(SUM(i.totalAmount), 0) " +
           "FROM Invoice i WHERE i.user.id = :userId GROUP BY i.status")
    List<Object[]> getInvoiceStatsByStatus(@Param("userId") Long userId);

    @Query("SELECT EXTRACT(MONTH FROM i.paidAt), EXTRACT(YEAR FROM i.paidAt), " +
           "COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE " +
           "i.user.id = :userId AND i.status = 'PAID' AND i.paidAt >= :startDate " +
           "GROUP BY EXTRACT(YEAR FROM i.paidAt), EXTRACT(MONTH FROM i.paidAt) " +
           "ORDER BY EXTRACT(YEAR FROM i.paidAt) DESC, EXTRACT(MONTH FROM i.paidAt) DESC")
    List<Object[]> getMonthlyRevenue(@Param("userId") Long userId,
                                     @Param("startDate") LocalDateTime startDate);

}