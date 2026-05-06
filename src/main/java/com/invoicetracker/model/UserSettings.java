package com.invoicetracker.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_settings_seq_gen")
//    @SequenceGenerator(name = "user_settings_seq_gen", sequenceName = "user_settings_seq", allocationSize = 1)
//    private Long id;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Userr user;

    @Column(name = "default_currency", length = 3)
    @Builder.Default
    private String defaultCurrency = "USD";

    @Column(name = "default_tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal defaultTaxRate = BigDecimal.ZERO;

    @Lob
    @Column(name = "default_payment_terms")
    private String defaultPaymentTerms;

    @Column(name = "invoice_number_prefix", length = 20)
    @Builder.Default
    private String invoiceNumberPrefix = "INV";

    @Column(name = "next_invoice_number")
    @Builder.Default
    private Integer nextInvoiceNumber = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_template_id")
    private InvoiceTemplate defaultTemplate;

    @Column(name = "send_payment_reminders")
    @Builder.Default
    private Boolean sendPaymentReminders = true;

    @Column(name = "reminder_days_before")
    @Builder.Default
    private Integer reminderDaysBefore = 7;

    @Column(name = "reminder_days_after")
    @Builder.Default
    private Integer reminderDaysAfter = 3;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getNextInvoiceNumberFormatted() {
        String prefix = invoiceNumberPrefix != null ? invoiceNumberPrefix : "INV";
        return String.format("%s-%05d", prefix, nextInvoiceNumber);
    }

    public void incrementInvoiceNumber() {
        this.nextInvoiceNumber++;
    }

}