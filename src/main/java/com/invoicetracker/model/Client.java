package com.invoicetracker.model;

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
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clients_seq_gen")
    @SequenceGenerator(name = "clients_seq_gen", sequenceName = "clients_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Userr user;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(name = "company_name")
    private String companyName;

    @Lob
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Column(name = "tax_id", length = 100)
    private String taxId;

    @Lob
    private String notes;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();
}