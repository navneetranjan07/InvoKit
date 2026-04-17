package com.invoicetracker.model.enums;

public enum SubscriptionStatus {
    ACTIVE("Active"),
    CANCELLED("Cancelled"),
    PAST_DUE("Past Due"),
    TRIAL("Trial");

    private final String displayName;

    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}