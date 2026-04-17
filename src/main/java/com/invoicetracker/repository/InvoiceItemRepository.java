package com.invoicetracker.repository;

import com.invoicetracker.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    List<InvoiceItem> findByInvoiceIdOrderBySortOrderAsc(Long invoiceId);

    long countByInvoiceId(Long invoiceId);

    @Query("SELECT COALESCE(SUM(ii.amount), 0) FROM InvoiceItem ii WHERE ii.invoice.id = :invoiceId")
    BigDecimal calculateTotalForInvoice(@Param("invoiceId") Long invoiceId);

    @Query("SELECT DISTINCT ii.description FROM InvoiceItem ii WHERE " +
           "ii.invoice.user.id = :userId AND " +
           "LOWER(ii.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<String> findDistinctDescriptions(@Param("userId") Long userId,
                                          @Param("search") String search);

    void deleteByInvoiceId(Long invoiceId);
}