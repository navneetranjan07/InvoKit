package com.invoicetracker.service;

import com.invoicetracker.dto.request.CreateInvoiceRequest;
import com.invoicetracker.dto.request.UpdateInvoiceRequest;
import com.invoicetracker.dto.response.InvoiceResponse;
import com.invoicetracker.dto.response.PageResponse;
import com.invoicetracker.exception.BadRequestException;
import com.invoicetracker.exception.ResourceNotFoundException;
import com.invoicetracker.exception.SubscriptionLimitException;
import com.invoicetracker.model.*;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.model.enums.RecurringFrequency;
import com.invoicetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @Autowired
    private EmailService emailService;

    // ==========================================
    // CREATE INVOICE
    // ==========================================
    @Transactional
    public InvoiceResponse createInvoice(Long userId, CreateInvoiceRequest request) {
        Userr user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Check monthly invoice limit
        if (user.getSubscriptionTier().hasInvoiceLimit()) {
            LocalDateTime startOfMonth = LocalDate.now()
                    .withDayOfMonth(1).atStartOfDay();
            long count = invoiceRepository
                    .countByUserIdAndCreatedAtAfter(userId, startOfMonth);
            if (count >= user.getSubscriptionTier().getMaxInvoicesPerMonth()) {
                throw new SubscriptionLimitException(
                        "Monthly invoice limit reached. Upgrade to Pro for unlimited invoices."
                );
            }
        }

        Client client = clientRepository.findByIdAndUserId(request.getClientId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        UserSettings settings = getOrCreateSettings(user);

        // Validate dates
        if (request.getDueDate().isBefore(request.getIssueDate())) {
            throw new BadRequestException("Due date cannot be before issue date");
        }

        Invoice invoice = Invoice.builder()
                .user(user)
                .client(client)
                .invoiceNumber(settings.getNextInvoiceNumberFormatted())
                .title(request.getTitle())
                .status(InvoiceStatus.DRAFT)
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : settings.getDefaultTaxRate())
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : settings.getDefaultCurrency())
                .notes(request.getNotes())
                .terms(request.getTerms() != null ? request.getTerms() : settings.getDefaultPaymentTerms())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .recurringFrequency(request.getRecurringFrequency())
                .build();

        // Add line items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int sortOrder = 0;
            for (var itemReq : request.getItems()) {
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .sortOrder(itemReq.getSortOrder() != null ? itemReq.getSortOrder() : sortOrder++)
                        .build();
                item.calculateAmount();
                invoice.getItems().add(item);
            }
        }

        invoice.calculateTotals();

        // Set next invoice date for recurring
        if (Boolean.TRUE.equals(invoice.getIsRecurring()) && invoice.getRecurringFrequency() != null) {
            invoice.setNextInvoiceDate(
                    invoice.getRecurringFrequency().getNextDate(invoice.getIssueDate())
            );
        }

        invoice = invoiceRepository.save(invoice);

        // Increment invoice number in settings
        settings.incrementInvoiceNumber();
        userSettingsRepository.save(settings);

        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // UPDATE INVOICE
    // ==========================================
    @Transactional
    public InvoiceResponse updateInvoice(Long userId, Long invoiceId, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (!invoice.getStatus().isEditable()) {
            throw new BadRequestException("Only DRAFT invoices can be edited");
        }

        Client client = clientRepository.findByIdAndUserId(request.getClientId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        if (request.getDueDate().isBefore(request.getIssueDate())) {
            throw new BadRequestException("Due date cannot be before issue date");
        }

        invoice.setClient(client);
        invoice.setTitle(request.getTitle());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        invoice.setTaxRate(request.getTaxRate() != null ? request.getTaxRate() : BigDecimal.ZERO);
        invoice.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        invoice.setCurrency(request.getCurrency());
        invoice.setNotes(request.getNotes());
        invoice.setTerms(request.getTerms());
        invoice.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        invoice.setRecurringFrequency(request.getRecurringFrequency());

        // Replace all items
        invoice.getItems().clear();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int sortOrder = 0;
            for (var itemReq : request.getItems()) {
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .sortOrder(itemReq.getSortOrder() != null ? itemReq.getSortOrder() : sortOrder++)
                        .build();
                item.calculateAmount();
                invoice.getItems().add(item);
            }
        }

        invoice.calculateTotals();
        invoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // GET INVOICE
    // ==========================================
    public InvoiceResponse getInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // GET ALL INVOICES
    // ==========================================
    public PageResponse<InvoiceResponse> getAllInvoices(Long userId, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(page.map(InvoiceResponse::fromEntitySimple));
    }

    // ==========================================
    // GET INVOICES BY STATUS
    // ==========================================
    public PageResponse<InvoiceResponse> getInvoicesByStatus(
            Long userId, InvoiceStatus status, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByUserIdAndStatus(userId, status, pageable);
        return PageResponse.fromPage(page.map(InvoiceResponse::fromEntitySimple));
    }

    // ==========================================
    // SEARCH INVOICES
    // ==========================================
    public PageResponse<InvoiceResponse> searchInvoices(Long userId, String search, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.searchInvoices(userId, search, pageable);
        return PageResponse.fromPage(page.map(InvoiceResponse::fromEntitySimple));
    }

    // ==========================================
    // SEND INVOICE
    // ==========================================
    @Transactional
    public InvoiceResponse sendInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (!invoice.getStatus().canBeSent()) {
            throw new BadRequestException("Invoice cannot be sent. Status: " + invoice.getStatus());
        }

        if (invoice.getClient().getEmail() == null || invoice.getClient().getEmail().isEmpty()) {
            throw new BadRequestException("Client does not have an email address");
        }

        if (invoice.getItems().isEmpty()) {
            throw new BadRequestException("Invoice has no line items");
        }

        // Generate PDF
        byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);

        // Send email with PDF
        emailService.sendInvoiceEmail(invoice, pdfBytes);

        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // MARK AS PAID
    // ==========================================
    @Transactional
    public InvoiceResponse markAsPaid(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (!invoice.getStatus().canBePaid()) {
            throw new BadRequestException("Invoice cannot be marked as paid. Status: " + invoice.getStatus());
        }

        invoice.markAsPaid();
        invoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // CANCEL INVOICE
    // ==========================================
    @Transactional
    public InvoiceResponse cancelInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (!invoice.getStatus().canBeCancelled()) {
            throw new BadRequestException("Invoice cannot be cancelled. Status: " + invoice.getStatus());
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);
        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // DELETE INVOICE
    // ==========================================
    @Transactional
    public void deleteInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT invoices can be deleted");
        }

        invoiceRepository.delete(invoice);
    }

    // ==========================================
    // DUPLICATE INVOICE
    // ==========================================
    @Transactional
    public InvoiceResponse duplicateInvoice(Long userId, Long invoiceId) {
        Invoice original = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        Userr user = original.getUser();
        UserSettings settings = getOrCreateSettings(user);

        Invoice copy = Invoice.builder()
                .user(user)
                .client(original.getClient())
                .invoiceNumber(settings.getNextInvoiceNumberFormatted())
                .title(original.getTitle() != null ? "Copy of " + original.getTitle() : null)
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .taxRate(original.getTaxRate())
                .discountAmount(original.getDiscountAmount())
                .currency(original.getCurrency())
                .notes(original.getNotes())
                .terms(original.getTerms())
                .isRecurring(false)
                .build();

        // Copy items
        for (InvoiceItem originalItem : original.getItems()) {
            InvoiceItem newItem = InvoiceItem.builder()
                    .invoice(copy)
                    .description(originalItem.getDescription())
                    .quantity(originalItem.getQuantity())
                    .unitPrice(originalItem.getUnitPrice())
                    .sortOrder(originalItem.getSortOrder())
                    .build();
            newItem.calculateAmount();
            copy.getItems().add(newItem);
        }

        copy.calculateTotals();
        Invoice saved = invoiceRepository.save(copy);

        settings.incrementInvoiceNumber();
        userSettingsRepository.save(settings);

        return InvoiceResponse.fromEntity(saved);
    }

    // ==========================================
    // DOWNLOAD PDF
    // ==========================================
    public byte[] downloadPdf(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        return pdfGenerationService.generateInvoicePdf(invoice);
    }

    // ==========================================
    // GET OVERDUE INVOICES
    // ==========================================
    public List<InvoiceResponse> getOverdueInvoices(Long userId) {
        return invoiceRepository.findOverdueInvoices(userId, LocalDate.now())
                .stream()
                .map(InvoiceResponse::fromEntitySimple)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET INVOICES BY CLIENT
    // ==========================================
    public List<InvoiceResponse> getInvoicesByClient(Long userId, Long clientId) {
        clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));
        return invoiceRepository.findByClientId(clientId)
                .stream()
                .map(InvoiceResponse::fromEntitySimple)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET RECENT INVOICES
    // ==========================================
    public List<InvoiceResponse> getRecentInvoices(Long userId, int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return invoiceRepository.findRecentInvoices(userId, pageable)
                .stream()
                .map(InvoiceResponse::fromEntitySimple)
                .collect(Collectors.toList());
    }

    // ==========================================
    // UPDATE OVERDUE STATUS (Scheduler)
    // ==========================================
    @Transactional
    public void updateOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository
                .findOverdueInvoices(null, LocalDate.now());
        overdueInvoices.forEach(invoice -> {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
        });
    }

    // ==========================================
    // HELPER
    // ==========================================
    private UserSettings getOrCreateSettings(Userr user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserSettings s = UserSettings.builder()
                            .user(user)
                            .build();
                    return userSettingsRepository.save(s);
                });
    }

    private void throwBadRequest(String message) {
        throw new BadRequestException(message);
    }
}