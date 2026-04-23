package com.invoicetracker.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_items_seq_gen")
    @SequenceGenerator(name = "invoice_items_seq_gen", sequenceName = "invoice_items_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal quantity = BigDecimal.ONE;

    @NotNull
    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void calculateAmount() {
        if (quantity != null && unitPrice != null) {
            this.amount = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        }
    }

}