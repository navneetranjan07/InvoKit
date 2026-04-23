package com.invoicetracker.model;

import com.invoicetracker.model.enums.SubscriptionStatus;
import com.invoicetracker.model.enums.SubscriptionTier;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscriptions_seq_gen")
    @SequenceGenerator(name = "subscriptions_seq_gen", sequenceName = "subscriptions_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Userr user;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 20)
    private SubscriptionTier planType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "cancel_at_period_end")
    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && (currentPeriodEnd == null || currentPeriodEnd.isAfter(LocalDateTime.now()));
    }

    public int getDaysUntilRenewal() {
        if (currentPeriodEnd == null) return -1;
        return (int) ChronoUnit.DAYS.between(LocalDateTime.now(), currentPeriodEnd);
    }

}