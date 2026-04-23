package com.invoicetracker.repository;

import com.invoicetracker.model.InvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceTemplateRepository extends JpaRepository<InvoiceTemplate, Long> {

    Optional<InvoiceTemplate> findByName(String name);

    List<InvoiceTemplate> findByIsPremiumFalse();

    List<InvoiceTemplate> findByIsPremiumTrue();

    List<InvoiceTemplate> findAllByOrderByCreatedAtDesc();

    boolean existsByName(String name);

    long countByIsPremiumFalse();

    long countByIsPremiumTrue();

}