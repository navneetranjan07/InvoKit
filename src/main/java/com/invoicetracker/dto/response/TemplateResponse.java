package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.InvoiceTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean isPremium;
    private String previewImageUrl;
    private LocalDateTime createdAt;

    // Full template (only when needed)
    private String htmlTemplate;
    private String cssStyles;

    // ========== Static Factory Methods ==========

    public static TemplateResponse fromEntity(InvoiceTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .isPremium(template.getIsPremium())
                .previewImageUrl(template.getPreviewImageUrl())
                .createdAt(template.getCreatedAt())
                .build();
    }

    public static TemplateResponse fromEntityWithContent(InvoiceTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .isPremium(template.getIsPremium())
                .previewImageUrl(template.getPreviewImageUrl())
                .htmlTemplate(template.getHtmlTemplate())
                .cssStyles(template.getCssStyles())
                .createdAt(template.getCreatedAt())
                .build();
    }
}