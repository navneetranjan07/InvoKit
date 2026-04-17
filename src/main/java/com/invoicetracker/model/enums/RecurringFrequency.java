package com.invoicetracker.model.enums;

import java.time.LocalDate;

public enum RecurringFrequency {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    YEARLY("Yearly");

    private final String displayName;

    RecurringFrequency(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public LocalDate getNextDate(LocalDate currentDate) {
        switch (this) {
            case WEEKLY: return currentDate.plusWeeks(1);
            case MONTHLY: return currentDate.plusMonths(1);
            case QUARTERLY: return currentDate.plusMonths(3);
            case YEARLY: return currentDate.plusYears(1);
            default: return currentDate;
        }
    }
}