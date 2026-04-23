package com.invoicetracker.dto.response;

import com.invoicetracker.model.enums.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String email;
    private String fullName;
    private String companyName;
    private SubscriptionTier subscriptionTier;

    public static AuthResponse of(String token, Long expiresIn, UserResponse user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .subscriptionTier(user.getSubscriptionTier())
                .build();
    }
}