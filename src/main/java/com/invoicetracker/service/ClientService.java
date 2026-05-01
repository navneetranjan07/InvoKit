package com.invoicetracker.service;

import com.invoicetracker.dto.request.CreateClientRequest;
import com.invoicetracker.dto.request.UpdateClientRequest;
import com.invoicetracker.dto.response.ClientResponse;
import com.invoicetracker.dto.response.PageResponse;
import com.invoicetracker.exception.BadRequestException;
import com.invoicetracker.exception.ResourceNotFoundException;
import com.invoicetracker.exception.SubscriptionLimitException;
import com.invoicetracker.model.Client;
import com.invoicetracker.model.Userr;
import com.invoicetracker.repository.ClientRepository;
import com.invoicetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ClientResponse createClient(Long userId, CreateClientRequest request) {
        Userr user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check subscription limit
        if (user.getSubscriptionTier().hasClientLimit()) {
            long activeClients = clientRepository.countByUserIdAndIsActiveTrue(userId);
            if (activeClients >= user.getSubscriptionTier().getMaxClients()) {
                throw new SubscriptionLimitException(
                        "You've reached your client limit of " +
                                user.getSubscriptionTier().getMaxClients() +
                                ". Upgrade to Pro for unlimited clients."
                );
            }
        }

        Client client = Client.builder()
                .user(user)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .companyName(request.getCompanyName())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .taxId(request.getTaxId())
                .notes(request.getNotes())
                .isActive(true)
                .build();

        client = clientRepository.save(client);
        return ClientResponse.fromEntity(client);
    }

    @Transactional
    public ClientResponse updateClient(Long userId, Long clientId, UpdateClientRequest request) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        client.setCompanyName(request.getCompanyName());
        client.setAddress(request.getAddress());
        client.setCity(request.getCity());
        client.setCountry(request.getCountry());
        client.setTaxId(request.getTaxId());
        client.setNotes(request.getNotes());

        if (request.getIsActive() != null) {
            client.setIsActive(request.getIsActive());
        }

        client = clientRepository.save(client);
        return ClientResponse.fromEntity(client);
    }

    public ClientResponse getClient(Long userId, Long clientId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
        return ClientResponse.fromEntity(client);
    }

    public PageResponse<ClientResponse> getAllClients(Long userId, Pageable pageable) {
        Page<Client> page = clientRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(page.map(ClientResponse::fromEntity));
    }

    public List<ClientResponse> getActiveClients(Long userId) {
        return clientRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(ClientResponse::fromEntitySimple)
                .collect(Collectors.toList());
    }

    public PageResponse<ClientResponse> searchClients(Long userId, String search, Pageable pageable) {
        Page<Client> page = clientRepository.searchClients(userId, search, pageable);
        return PageResponse.fromPage(page.map(ClientResponse::fromEntity));
    }

    @Transactional
    public void deleteClient(Long userId, Long clientId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        if (!client.getInvoices().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete client with existing invoices. Deactivate the client instead."
            );
        }

        clientRepository.delete(client);
    }

    @Transactional
    public ClientResponse deactivateClient(Long userId, Long clientId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
        client.setIsActive(false);
        client = clientRepository.save(client);
        return ClientResponse.fromEntity(client);
    }

    @Transactional
    public ClientResponse activateClient(Long userId, Long clientId) {
        Client client = clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
        client.setIsActive(true);
        client = clientRepository.save(client);
        return ClientResponse.fromEntity(client);
    }

    public long countActiveClients(Long userId) {
        return clientRepository.countByUserIdAndIsActiveTrue(userId);
    }

    public long countTotalClients(Long userId) {
        return clientRepository.countByUserId(userId);
    }
}