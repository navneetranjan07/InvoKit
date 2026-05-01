package com.invoicetracker.service;

import com.invoicetracker.dto.request.RecordPaymentRequest;
import com.invoicetracker.dto.response.PageResponse;
import com.invoicetracker.dto.response.PaymentResponse;
import com.invoicetracker.exception.BadRequestException;
import com.invoicetracker.exception.ResourceNotFoundException;
import com.invoicetracker.model.Invoice;
import com.invoicetracker.model.Payment;
import com.invoicetracker.model.enums.InvoiceStatus;
import com.invoicetracker.repository.InvoiceRepository;
import com.invoicetracker.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // ==========================================
    // RECORD PAYMENT
    // ==========================================
    @Transactional
    public PaymentResponse recordPayment(Long userId, RecordPaymentRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(request.getInvoiceId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", request.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BadRequestException("Invoice is already fully paid");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new BadRequestException("Cannot record payment for a cancelled invoice");
        }

        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new BadRequestException("Cannot record payment for a draft invoice");
        }

        BigDecimal balanceDue = invoice.getBalanceDue();
        if (request.getAmount().compareTo(balanceDue) > 0) {
            throw new BadRequestException(
                    "Payment amount (" + request.getAmount() +
                            ") exceeds balance due (" + balanceDue + ")"
            );
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentMethod(request.getPaymentMethod())
                .referenceNumber(request.getReferenceNumber())
                .notes(request.getNotes())
                .build();

        payment = paymentRepository.save(payment);

        // Update invoice
        invoice.addPayment(request.getAmount());
        invoiceRepository.save(invoice);

        return PaymentResponse.fromEntity(payment);
    }

    // ==========================================
    // GET PAYMENTS FOR INVOICE
    // ==========================================
    public List<PaymentResponse> getPaymentsByInvoice(Long userId, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndUserId(invoiceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceId));

        return paymentRepository.findByInvoiceIdOrderByPaymentDateDesc(invoice.getId())
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==========================================
    // GET ALL PAYMENTS FOR USER
    // ==========================================
    public PageResponse<PaymentResponse> getAllPayments(Long userId, Pageable pageable) {
        Page<Payment> page = paymentRepository.findByUserId(userId, pageable);
        return PageResponse.fromPage(page.map(PaymentResponse::fromEntity));
    }

    // ==========================================
    // GET RECENT PAYMENTS
    // ==========================================
    public List<PaymentResponse> getRecentPayments(Long userId, int limit) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, limit);
        return paymentRepository.findRecentPayments(userId, pageable)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ==========================================
    // DELETE PAYMENT
    // ==========================================
    @Transactional
    public void deletePayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        Invoice invoice = payment.getInvoice();

        if (!invoice.getUser().getId().equals(userId)) {
            throw new BadRequestException("Access denied");
        }

        // Reverse the payment
        invoice.setAmountPaid(invoice.getAmountPaid().subtract(payment.getAmount()));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            invoice.setStatus(InvoiceStatus.SENT);
            invoice.setPaidAt(null);
        }
        invoiceRepository.save(invoice);
        paymentRepository.delete(payment);
    }
}