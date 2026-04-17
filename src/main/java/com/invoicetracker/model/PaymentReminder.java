package com.invoicetracker.model;

import com.invoicetracker.model.enums.ReminderType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_reminders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_reminders_seq_gen")
    @SequenceGenerator(name = "payment_reminders_seq_gen", sequenceName = "payment_reminders_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 20)
    private ReminderType reminderType;

    @NotNull
    @Column(name = "days_offset", nullable = false)
    private Integer daysOffset;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @NotNull
    @Column(name = "scheduled_for", nullable = false)
    private LocalDateTime scheduledFor;

    @Column(name = "email_subject")
    private String emailSubject;

    @Lob
    @Column(name = "email_body")
    private String emailBody;

    @Column(name = "is_sent")
    @Builder.Default
    private Boolean isSent = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
    }

    public boolean shouldBeSent() {
        return !isSent && LocalDateTime.now().isAfter(scheduledFor);
    }
}