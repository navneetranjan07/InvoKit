package com.invoicetracker.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.invoicetracker.model.Client;
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
public class ClientResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String companyName;
    private String address;
    private String city;
    private String country;
    private String taxId;
    private String notes;
    private Boolean isActive;
    private Long totalInvoices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ========== Static Factory Method ==========

    public static ClientResponse fromEntity(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .companyName(client.getCompanyName())
                .address(client.getAddress())
                .city(client.getCity())
                .country(client.getCountry())
                .taxId(client.getTaxId())
                .notes(client.getNotes())
                .isActive(client.getIsActive())
                .totalInvoices((long) client.getInvoices().size())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    public static ClientResponse fromEntitySimple(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .companyName(client.getCompanyName())
                .city(client.getCity())
                .country(client.getCountry())
                .isActive(client.getIsActive())
                .build();
    }
}