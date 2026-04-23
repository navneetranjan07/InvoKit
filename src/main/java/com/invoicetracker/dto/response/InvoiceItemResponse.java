package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.InvoiceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceItemResponse {

    private Long id;
    private String description;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private Integer sortOrder;

    // ========== Static Factory Method ==========

    public static InvoiceItemResponse fromEntity(InvoiceItem item) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .sortOrder(item.getSortOrder())
                .build();
    }
}