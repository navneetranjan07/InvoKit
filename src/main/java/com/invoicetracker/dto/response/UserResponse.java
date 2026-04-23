package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.User;
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
public class UserResponse {

    private Long id;
    private String email;
    private String fullName;
    private String companyName;
    private String companyLogoUrl;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String taxId;
    private SubscriptionTier subscriptionTier;
    private LocalDateTime trialEndsAt;
    private LocalDateTime createdAt;

    // ========== Static Factory Method ==========

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .companyLogoUrl(user.getCompanyLogoUrl())
                .phone(user.getPhone())
                .address(user.getAddress())
                .city(user.getCity())
                .country(user.getCountry())
                .taxId(user.getTaxId())
                .subscriptionTier(user.getSubscriptionTier())
                .trialEndsAt(user.getTrialEndsAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}