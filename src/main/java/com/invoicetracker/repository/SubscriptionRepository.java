package com.invoicetracker.repository;

import com.invoicetracker.model.Subscription;
import com.invoicetracker.model.enums.SubscriptionStatus;
import com.invoicetracker.model.enums.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' " +
           "ORDER BY s.createdAt DESC")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByPlanType(SubscriptionTier planType);

    @Query("SELECT s FROM Subscription s WHERE s.cancelAtPeriodEnd = true AND " +
           "s.currentPeriodEnd <= :now")
    List<Subscription> findSubscriptionsToCancel(@Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Subscription s " +
           "WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    boolean hasActiveSubscription(@Param("userId") Long userId);

    long countByStatus(SubscriptionStatus status);

    long countByPlanType(SubscriptionTier planType);

    void deleteByUserId(Long userId);

}