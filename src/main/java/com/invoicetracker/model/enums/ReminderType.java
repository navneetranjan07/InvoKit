package com.invoicetracker.model.enums;

public enum ReminderType {
    BEFORE_DUE("Before Due Date"),
    ON_DUE("On Due Date"),
    AFTER_DUE("After Due Date");

    private final String displayName;

    ReminderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}