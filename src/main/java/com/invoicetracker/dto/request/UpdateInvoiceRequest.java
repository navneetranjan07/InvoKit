package com.invoicetracker.dto.request;

import com.invoicetracker.model.enums.RecurringFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInvoiceRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @DecimalMin(value = "0.0", message = "Tax rate must be 0 or greater")
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Discount amount must be 0 or greater")
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Size(max = 3, message = "Currency must be 3 characters")
    @Builder.Default
    private String currency = "USD";

    private String notes;

    private String terms;

    @Builder.Default
    private Boolean isRecurring = false;

    private RecurringFrequency recurringFrequency;

    @Valid
    @Builder.Default
    private List<InvoiceItemRequest> items = new ArrayList<>();
}