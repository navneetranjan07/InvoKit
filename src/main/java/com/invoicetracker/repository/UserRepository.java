package com.invoicetracker.repository;

import com.invoicetracker.model.Userr;
import com.invoicetracker.model.enums.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Userr, Long> {

    Optional<Userr> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Userr> findByStripeCustomerId(String stripeCustomerId);

    List<Userr> findBySubscriptionTier(SubscriptionTier tier);

    long countBySubscriptionTier(SubscriptionTier tier);

}