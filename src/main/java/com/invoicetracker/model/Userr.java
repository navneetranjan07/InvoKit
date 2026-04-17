package com.invoicetracker.model;

import com.invoicetracker.model.enums.SubscriptionTier;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Userr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq_gen")
    @SequenceGenerator(name = "users_seq_gen", sequenceName = "users_seq", allocationSize = 1)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_logo_url", length = 500)
    private String companyLogoUrl;

    @Column(length = 50)
    private String phone;

    @Lob
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", length = 20)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Client> clients = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSettings settings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subscription> subscriptions = new ArrayList<>();
}