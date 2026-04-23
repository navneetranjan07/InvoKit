package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.Subscription;
import com.invoicetracker.model.enums.SubscriptionStatus;
import com.invoicetracker.model.enums.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionResponse {

    private Long id;
    private SubscriptionTier planType;
    private String planDisplayName;
    private SubscriptionStatus status;
    private String statusDisplayName;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;
    private Integer daysUntilRenewal;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // ========== Static Factory Method ==========

    public static SubscriptionResponse fromEntity(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .planType(subscription.getPlanType())
                .planDisplayName(subscription.getPlanType().getDisplayName())
                .status(subscription.getStatus())
                .statusDisplayName(subscription.getStatus().getDisplayName())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .daysUntilRenewal(subscription.getDaysUntilRenewal())
                .isActive(subscription.isActive())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}