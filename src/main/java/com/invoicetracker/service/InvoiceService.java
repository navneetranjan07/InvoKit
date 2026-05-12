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
import com.invoicetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
            LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            long count = invoiceRepository.countByUserIdAndCreatedAtAfter(userId, startOfMonth);
            if (count >= user.getSubscriptionTier().getMaxInvoicesPerMonth()) {
                throw new SubscriptionLimitException("Monthly invoice limit reached.");
            }
        }

        Client client = clientRepository.findByIdAndUserId(request.getClientId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        UserSettings settings = getOrCreateSettings(user);

        // Unique number per users
        String invoiceNum = settings.getNextInvoiceNumberFormatted();
        while (invoiceRepository.existsByInvoiceNumberAndUserId(invoiceNum, userId)) {
            settings.incrementInvoiceNumber();
            invoiceNum = settings.getNextInvoiceNumberFormatted();
        }

        // Validate dates
        if (request.getDueDate().isBefore(request.getIssueDate())) {
            throw new BadRequestException("Due date cannot be before issue date");
        }

        Invoice invoice = Invoice.builder()
                .user(user)
                .client(client)
                .invoiceNumber(invoiceNum)
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
            int currentOrder = 0;
            for (var itemReq : request.getItems()) {
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .sortOrder(itemReq.getSortOrder() != null ? itemReq.getSortOrder() : currentOrder++)
                        .build();
                item.calculateAmount();
                invoice.getItems().add(item);
            }
        }

        invoice.calculateTotals();

        if (Boolean.TRUE.equals(invoice.getIsRecurring()) && invoice.getRecurringFrequency() != null) {
            invoice.setNextInvoiceDate(invoice.getRecurringFrequency().getNextDate(invoice.getIssueDate()));
        }

        invoice = invoiceRepository.save(invoice);

        // Increment and save settings
        settings.incrementInvoiceNumber();
        userSettingsRepository.save(settings);

        return InvoiceResponse.fromEntity(invoice);
    }

    // ==========================================
    // DUPLICATE INVOICE
    // ==========================================
    @Transactional
    public InvoiceResponse duplicateInvoice(Long userId, Long invoiceId) {
        Invoice original = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        UserSettings settings = getOrCreateSettings(original.getUser());

        // --- APPLY UNIQUE FIX HERE AS WELL ---
        String invoiceNum = settings.getNextInvoiceNumberFormatted();
        while (invoiceRepository.existsByInvoiceNumberAndUserId(invoiceNum, userId)) {
            settings.incrementInvoiceNumber();
            invoiceNum = settings.getNextInvoiceNumberFormatted();
        }

        Invoice copy = Invoice.builder()
                .user(original.getUser())
                .client(original.getClient())
                .invoiceNumber(invoiceNum)
                .title(original.getTitle() != null ? "Copy of " + original.getTitle() : "Duplicate Invoice")
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
    // PRESERVED FEATURES
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

        invoice.getItems().clear();
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            int currentOrder = 0;
            for (var itemReq : request.getItems()) {
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemReq.getDescription())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(itemReq.getUnitPrice())
                        .sortOrder(itemReq.getSortOrder() != null ? itemReq.getSortOrder() : currentOrder++)
                        .build();
                item.calculateAmount();
                invoice.getItems().add(item);
            }
        }

        invoice.calculateTotals();
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }

    public InvoiceResponse getInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));
        return InvoiceResponse.fromEntity(invoice);
    }

    public PageResponse<InvoiceResponse> getAllInvoices(Long userId, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(page.map(InvoiceResponse::fromEntitySimple));
    }

    public PageResponse<InvoiceResponse> getInvoicesByStatus(Long userId, InvoiceStatus status, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByUserIdAndStatus(userId, status, pageable);
        return PageResponse.fromPage(page.map(InvoiceResponse::fromEntitySimple));
    }

    public PageResponse<InvoiceResponse> searchInvoices(Long userId, String search, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.searchInvoices(userId, search, pageable);
        return PageResponse.fromPage(page.map(InvoiceResponse::fromEntitySimple));
    }

    @Transactional
    public InvoiceResponse sendInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId).orElseThrow();
        if (!invoice.getStatus().canBeSent()) throw new BadRequestException("Invoice cannot be sent.");

        byte[] pdfBytes = pdfGenerationService.generateInvoicePdf(invoice);
        emailService.sendInvoiceEmail(invoice, pdfBytes);
        invoice.markAsSent();
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse markAsPaid(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId).orElseThrow();
        invoice.markAsPaid();
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse cancelInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId).orElseThrow();
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return InvoiceResponse.fromEntity(invoiceRepository.save(invoice));
    }

    @Transactional
    public void deleteInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId).orElseThrow();
        if (invoice.getStatus() != InvoiceStatus.DRAFT) throw new BadRequestException("Only DRAFT can be deleted");
        invoiceRepository.delete(invoice);
    }

    public byte[] downloadPdf(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId).orElseThrow();
        return pdfGenerationService.generateInvoicePdf(invoice);
    }

    public List<InvoiceResponse> getOverdueInvoices(Long userId) {
        return invoiceRepository.findOverdueInvoices(userId, LocalDate.now())
                .stream().map(InvoiceResponse::fromEntitySimple).collect(Collectors.toList());
    }

    public List<InvoiceResponse> getRecentInvoices(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return invoiceRepository.findRecentInvoices(userId, pageable)
                .stream().map(InvoiceResponse::fromEntitySimple).collect(Collectors.toList());
    }

    @Transactional
    public void updateOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices(null, LocalDate.now());
        overdue.forEach(i -> { i.setStatus(InvoiceStatus.OVERDUE); invoiceRepository.save(i); });
    }

    private UserSettings getOrCreateSettings(Userr user) {
        return userSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> userSettingsRepository.save(UserSettings.builder().user(user).build()));
    }

    // Add this to InvoiceService.java if missing
    public List<InvoiceResponse> getInvoicesByClient(Long userId, Long clientId) {
        // Verify client belongs to user first
        clientRepository.findByIdAndUserId(clientId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        return invoiceRepository.findByClientId(clientId)
                .stream()
                .map(InvoiceResponse::fromEntitySimple)
                .collect(Collectors.toList());
    }
}