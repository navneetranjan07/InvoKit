package com.invoicetracker.model.enums;

public enum InvoiceStatus {
    DRAFT("Draft"),
    SENT("Sent"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
    public boolean isEditable() { return this == DRAFT; }
    public boolean canBeSent() { return this == DRAFT; }
    public boolean canBePaid() { return this == SENT || this == OVERDUE; }
    public boolean canBeCancelled() { return this != PAID && this != CANCELLED; }
}