package com.invoicetracker.model.enums;

public enum SubscriptionTier {
    FREE("Free", 5, 20),
    PRO("Pro", -1, -1),
    PREMIUM("Premium", -1, -1);

    private final String displayName;
    private final int maxClients;
    private final int maxInvoicesPerMonth;

    SubscriptionTier(String displayName, int maxClients, int maxInvoicesPerMonth) {
        this.displayName = displayName;
        this.maxClients = maxClients;
        this.maxInvoicesPerMonth = maxInvoicesPerMonth;
    }

    public String getDisplayName() { return displayName; }
    public int getMaxClients() { return maxClients; }
    public int getMaxInvoicesPerMonth() { return maxInvoicesPerMonth; }
    public boolean hasClientLimit() { return maxClients > 0; }
    public boolean hasInvoiceLimit() { return maxInvoicesPerMonth > 0; }
}