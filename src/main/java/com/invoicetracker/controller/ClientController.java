package com.invoicetracker.controller;

import com.invoicetracker.dto.request.CreateClientRequest;
import com.invoicetracker.dto.request.UpdateClientRequest;
import com.invoicetracker.dto.response.ApiResponse;
import com.invoicetracker.dto.response.ClientResponse;
import com.invoicetracker.dto.response.PageResponse;
import com.invoicetracker.security.SecurityUtils;
import com.invoicetracker.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @Valid @RequestBody CreateClientRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        ClientResponse client = clientService.createClient(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Client created successfully", client));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        ClientResponse client = clientService.updateClient(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", client));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> getClient(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        ClientResponse client = clientService.getClient(userId, id);
        return ResponseEntity.ok(ApiResponse.success(client));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClientResponse>>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search) {
        Long userId = securityUtils.getCurrentUserId();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        PageResponse<ClientResponse> clients = search != null && !search.isEmpty()
                ? clientService.searchClients(userId, search, pageable)
                : clientService.getAllClients(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getActiveClients() {
        Long userId = securityUtils.getCurrentUserId();
        List<ClientResponse> clients = clientService.getActiveClients(userId);
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        clientService.deleteClient(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<ClientResponse>> deactivateClient(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        ClientResponse client = clientService.deactivateClient(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Client deactivated", client));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<ClientResponse>> activateClient(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        ClientResponse client = clientService.activateClient(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Client activated", client));
    }
}