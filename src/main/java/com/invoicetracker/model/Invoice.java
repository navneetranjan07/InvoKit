package com.invoicetracker.model;

import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.model.enums.RecurringFrequency;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoices_seq_gen")
    @SequenceGenerator(name = "invoices_seq_gen", sequenceName = "invoices_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Userr user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @NotNull
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @NotNull
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Lob
    private String notes;

    @Lob
    private String terms;

    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurring_frequency", length = 20)
    private RecurringFrequency recurringFrequency;

    @Column(name = "next_invoice_date")
    private LocalDate nextInvoiceDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentReminder> reminders = new ArrayList<>();

    // ========== Business Logic ==========

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(InvoiceItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        this.taxAmount = subtotal
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        this.totalAmount = subtotal
                .add(taxAmount)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(amountPaid).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isOverdue() {
        return status != InvoiceStatus.PAID
                && status != InvoiceStatus.CANCELLED
                && LocalDate.now().isAfter(dueDate);
    }

    public boolean isPaid() {
        return status == InvoiceStatus.PAID
                || amountPaid.compareTo(totalAmount) >= 0;
    }

    public boolean isPartiallyPaid() {
        return amountPaid.compareTo(BigDecimal.ZERO) > 0
                && amountPaid.compareTo(totalAmount) < 0;
    }

    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.amountPaid = this.totalAmount;
    }

    public void markAsSent() {
        if (this.status == InvoiceStatus.DRAFT) {
            this.status = InvoiceStatus.SENT;
            this.sentAt = LocalDateTime.now();
        }
    }

    public void addPayment(BigDecimal paymentAmount) {
        this.amountPaid = this.amountPaid.add(paymentAmount);
        if (this.amountPaid.compareTo(this.totalAmount) >= 0) {
            markAsPaid();
        }
    }

    public int getDaysOverdue() {
        if (!isOverdue()) return 0;
        return (int) ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public void addItem(InvoiceItem item) {
        items.add(item);
        item.setInvoice(this);
        calculateTotals();
    }

    public void removeItem(InvoiceItem item) {
        items.remove(item);
        item.setInvoice(null);
        calculateTotals();
    }

}