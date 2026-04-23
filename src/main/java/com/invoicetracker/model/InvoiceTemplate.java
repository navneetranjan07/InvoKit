package com.invoicetracker.model;

import com.invoicetracker.model.enums.SubscriptionTier;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_templates_seq_gen")
    @SequenceGenerator(name = "invoice_templates_seq_gen", sequenceName = "invoice_templates_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Lob
    @Column(name = "html_template", nullable = false)
    private String htmlTemplate;

    @Lob
    @Column(name = "css_styles")
    private String cssStyles;

    @Column(name = "is_premium")
    @Builder.Default
    private Boolean isPremium = false;

    @Column(name = "preview_image_url", length = 500)
    private String previewImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public boolean isAvailableForTier(SubscriptionTier tier) {
        if (!isPremium) return true;
        return tier == SubscriptionTier.PRO || tier == SubscriptionTier.PREMIUM;
    }

}