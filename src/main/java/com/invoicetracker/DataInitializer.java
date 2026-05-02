package com.invoicetracker;

import com.invoicetracker.model.*;
import com.invoicetracker.model.enums.*;
import com.invoicetracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only run if no users exist
        if (userRepository.count() > 0) {
            System.out.println("✅ Data already initialized, skipping...");
            return;
        }

        System.out.println("🚀 Initializing test data...");

        // Create test user
        Userr user = Userr.builder()
                .email("test@invokit.com")
                .passwordHash(passwordEncoder.encode("Test@1234"))
                .fullName("John Freelancer")
                .companyName("JF Digital Studio")
                .phone("+1234567890")
                .subscriptionTier(SubscriptionTier.FREE)
                .trialEndsAt(LocalDateTime.now().plusDays(14))
                .build();
        user = userRepository.save(user);

        // Create default settings
        UserSettings settings = UserSettings.builder()
                .user(user)
                .defaultCurrency("USD")
                .defaultTaxRate(new BigDecimal("10.00"))
                .defaultPaymentTerms("Payment due within 15 days of invoice date.")
                .invoiceNumberPrefix("INV")
                .nextInvoiceNumber(1)
                .sendPaymentReminders(true)
                .reminderDaysBefore(7)
                .reminderDaysAfter(3)
                .build();
        userSettingsRepository.save(settings);

        // Create clients
        Client alice = Client.builder()
                .user(user)
                .name("Alice Johnson")
                .email("alice@example.com")
                .phone("+1987654321")
                .companyName("Alice Tech Solutions")
                .city("New York")
                .country("USA")
                .isActive(true)
                .build();
        alice = clientRepository.save(alice);

        Client bob = Client.builder()
                .user(user)
                .name("Bob Smith")
                .email("bob@example.com")
                .phone("+1122334455")
                .companyName("Bob Marketing Agency")
                .city("Los Angeles")
                .country("USA")
                .isActive(true)
                .build();
        bob = clientRepository.save(bob);

        Client carol = Client.builder()
                .user(user)
                .name("Carol White")
                .email("carol@example.com")
                .phone("+1555666777")
                .companyName("White Consulting Ltd")
                .city("Chicago")
                .country("USA")
                .isActive(true)
                .build();
        carol = clientRepository.save(carol);

        // Create invoices
        // Invoice 1 - PAID
        Invoice inv1 = Invoice.builder()
                .user(user)
                .client(alice)
                .invoiceNumber("INV-00001")
                .title("Website Development Project")
                .status(InvoiceStatus.PAID)
                .issueDate(LocalDate.now().minusDays(30))
                .dueDate(LocalDate.now().minusDays(15))
                .subtotal(new BigDecimal("1000.00"))
                .taxRate(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("100.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1100.00"))
                .amountPaid(new BigDecimal("1100.00"))
                .currency("USD")
                .paidAt(LocalDateTime.now().minusDays(10))
                .build();

        InvoiceItem item1 = InvoiceItem.builder()
                .invoice(inv1)
                .description("Frontend Development")
                .quantity(new BigDecimal("10"))
                .unitPrice(new BigDecimal("80.00"))
                .amount(new BigDecimal("800.00"))
                .sortOrder(0)
                .build();

        InvoiceItem item2 = InvoiceItem.builder()
                .invoice(inv1)
                .description("Backend API Development")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("200.00"))
                .amount(new BigDecimal("200.00"))
                .sortOrder(1)
                .build();

        inv1.getItems().add(item1);
        inv1.getItems().add(item2);
        invoiceRepository.save(inv1);

        // Invoice 2 - SENT
        Invoice inv2 = Invoice.builder()
                .user(user)
                .client(bob)
                .invoiceNumber("INV-00002")
                .title("Logo Design & Branding")
                .status(InvoiceStatus.SENT)
                .issueDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().plusDays(5))
                .subtotal(new BigDecimal("500.00"))
                .taxRate(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("50.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("550.00"))
                .amountPaid(BigDecimal.ZERO)
                .currency("USD")
                .sentAt(LocalDateTime.now().minusDays(10))
                .build();

        InvoiceItem item3 = InvoiceItem.builder()
                .invoice(inv2)
                .description("Logo Design")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("300.00"))
                .amount(new BigDecimal("300.00"))
                .sortOrder(0)
                .build();

        InvoiceItem item4 = InvoiceItem.builder()
                .invoice(inv2)
                .description("Brand Style Guide")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("200.00"))
                .amount(new BigDecimal("200.00"))
                .sortOrder(1)
                .build();

        inv2.getItems().add(item3);
        inv2.getItems().add(item4);
        invoiceRepository.save(inv2);

        // Invoice 3 - OVERDUE
        Invoice inv3 = Invoice.builder()
                .user(user)
                .client(carol)
                .invoiceNumber("INV-00003")
                .title("SEO Consulting Services")
                .status(InvoiceStatus.OVERDUE)
                .issueDate(LocalDate.now().minusDays(20))
                .dueDate(LocalDate.now().minusDays(5))
                .subtotal(new BigDecimal("750.00"))
                .taxRate(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("75.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("825.00"))
                .amountPaid(BigDecimal.ZERO)
                .currency("USD")
                .sentAt(LocalDateTime.now().minusDays(20))
                .build();

        InvoiceItem item5 = InvoiceItem.builder()
                .invoice(inv3)
                .description("SEO Audit & Strategy")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("500.00"))
                .amount(new BigDecimal("500.00"))
                .sortOrder(0)
                .build();

        InvoiceItem item6 = InvoiceItem.builder()
                .invoice(inv3)
                .description("Monthly SEO Consulting")
                .quantity(new BigDecimal("5"))
                .unitPrice(new BigDecimal("50.00"))
                .amount(new BigDecimal("250.00"))
                .sortOrder(1)
                .build();

        inv3.getItems().add(item5);
        inv3.getItems().add(item6);
        invoiceRepository.save(inv3);

        // Invoice 4 - DRAFT
        Invoice inv4 = Invoice.builder()
                .user(user)
                .client(alice)
                .invoiceNumber("INV-00004")
                .title("Mobile App Development")
                .status(InvoiceStatus.DRAFT)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(15))
                .subtotal(new BigDecimal("2000.00"))
                .taxRate(new BigDecimal("10.00"))
                .taxAmount(new BigDecimal("200.00"))
                .discountAmount(new BigDecimal("100.00"))
                .totalAmount(new BigDecimal("2100.00"))
                .amountPaid(BigDecimal.ZERO)
                .currency("USD")
                .build();

        InvoiceItem item7 = InvoiceItem.builder()
                .invoice(inv4)
                .description("iOS App Development")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("1200.00"))
                .amount(new BigDecimal("1200.00"))
                .sortOrder(0)
                .build();

        InvoiceItem item8 = InvoiceItem.builder()
                .invoice(inv4)
                .description("Android App Development")
                .quantity(BigDecimal.ONE)
                .unitPrice(new BigDecimal("800.00"))
                .amount(new BigDecimal("800.00"))
                .sortOrder(1)
                .build();

        inv4.getItems().add(item7);
        inv4.getItems().add(item8);
        invoiceRepository.save(inv4);

        // Update next invoice number
        settings.setNextInvoiceNumber(5);
        userSettingsRepository.save(settings);

        System.out.println("✅ Test data initialized successfully!");
        System.out.println("📧 Email: test@invokit.com");
        System.out.println("🔑 Password: Test@1234");
    }
}