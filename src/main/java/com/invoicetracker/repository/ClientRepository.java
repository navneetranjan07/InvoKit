package com.invoicetracker.repository;

import com.invoicetracker.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    List<Client> findByUserId(Long userId);

    Page<Client> findByUserId(Long userId, Pageable pageable);

    List<Client> findByUserIdAndIsActiveTrue(Long userId);

    Optional<Client> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsActiveTrue(Long userId);

    long countByUserId(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Client c WHERE c.user.id = :userId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> searchClients(@Param("userId") Long userId,
                               @Param("search") String search,
                               Pageable pageable);
}