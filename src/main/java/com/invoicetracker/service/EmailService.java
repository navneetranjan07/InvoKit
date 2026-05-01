package com.invoicetracker.service;

import com.invoicetracker.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ==========================================
    // SEND INVOICE EMAIL
    // ==========================================
    public void sendInvoiceEmail(Invoice invoice, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(invoice.getClient().getEmail());
            helper.setSubject("Invoice #" + invoice.getInvoiceNumber() + " from " +
                    getBusinessName(invoice));
            helper.setText(buildInvoiceEmailBody(invoice), true);

            // ✅ Fixed: ByteArrayResource from org.springframework.core.io
            helper.addAttachment(
                    "Invoice-" + invoice.getInvoiceNumber() + ".pdf",
                    new ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send invoice email: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // SEND PAYMENT REMINDER
    // ==========================================
    public void sendPaymentReminderEmail(Invoice invoice, String customSubject, String customBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(invoice.getClient().getEmail());
            helper.setSubject(customSubject != null ? customSubject :
                    "Payment Reminder: Invoice #" + invoice.getInvoiceNumber());
            helper.setText(customBody != null ? customBody :
                    buildReminderEmailBody(invoice), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send reminder email: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // SEND WELCOME EMAIL
    // ==========================================
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to InvoKit!");
            helper.setText(buildWelcomeEmailBody(fullName), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email: " + e.getMessage(), e);
        }
    }

    // ==========================================
    // EMAIL BODY BUILDERS
    // ==========================================
    private String buildInvoiceEmailBody(Invoice invoice) {
        String businessName = getBusinessName(invoice);
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: #2563EB; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">InvoKit</h1>
                </div>
                <div style="background: #F9FAFB; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #E5E7EB;">
                    <p style="font-size: 16px;">Dear <strong>%s</strong>,</p>
                    <p>Please find attached invoice <strong>#%s</strong> from <strong>%s</strong>.</p>
                    <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border: 1px solid #E5E7EB;">
                        <table style="width: 100%%;">
                            <tr>
                                <td style="color: #6B7280;">Invoice Number</td>
                                <td style="text-align: right; font-weight: bold;">#%s</td>
                            </tr>
                            <tr>
                                <td style="color: #6B7280;">Issue Date</td>
                                <td style="text-align: right;">%s</td>
                            </tr>
                            <tr>
                                <td style="color: #6B7280;">Due Date</td>
                                <td style="text-align: right; color: #DC2626;">%s</td>
                            </tr>
                            <tr>
                                <td style="color: #6B7280; padding-top: 10px; font-size: 18px; font-weight: bold;">Total Amount</td>
                                <td style="text-align: right; font-size: 18px; font-weight: bold; padding-top: 10px; color: #2563EB;">%s %s</td>
                            </tr>
                        </table>
                    </div>
                    <p style="color: #6B7280; font-size: 14px;">The invoice is attached as a PDF. Please process payment by the due date.</p>
                    <p>Thank you for your business!</p>
                    <p style="margin-bottom: 0;"><strong>%s</strong></p>
                </div>
                <p style="text-align: center; color: #9CA3AF; font-size: 12px; margin-top: 20px;">
                    Powered by InvoKit
                </p>
            </body>
            </html>
            """.formatted(
                invoice.getClient().getName(),
                invoice.getInvoiceNumber(),
                businessName,
                invoice.getInvoiceNumber(),
                invoice.getIssueDate().format(DATE_FORMAT),
                invoice.getDueDate().format(DATE_FORMAT),
                invoice.getCurrency(),
                invoice.getTotalAmount(),
                businessName
        );
    }

    private String buildReminderEmailBody(Invoice invoice) {
        boolean isOverdue = invoice.isOverdue();
        String businessName = getBusinessName(invoice);

        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: %s; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 24px;">Payment Reminder</h1>
                </div>
                <div style="background: #F9FAFB; padding: 30px; border-radius: 0 0 8px 8px; border: 1px solid #E5E7EB;">
                    <p style="font-size: 16px;">Dear <strong>%s</strong>,</p>
                    <p>%s</p>
                    <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border: 1px solid #E5E7EB;">
                        <table style="width: 100%%;">
                            <tr>
                                <td style="color: #6B7280;">Invoice Number</td>
                                <td style="text-align: right; font-weight: bold;">#%s</td>
                            </tr>
                            <tr>
                                <td style="color: #6B7280;">Due Date</td>
                                <td style="text-align: right; color: #DC2626;">%s</td>
                            </tr>
                            <tr>
                                <td style="color: #6B7280; padding-top: 10px; font-weight: bold;">Balance Due</td>
                                <td style="text-align: right; font-size: 18px; font-weight: bold; padding-top: 10px; color: #DC2626;">%s %s</td>
                            </tr>
                        </table>
                    </div>
                    <p>Please process your payment as soon as possible.</p>
                    <p style="margin-bottom: 0;"><strong>%s</strong></p>
                </div>
            </body>
            </html>
            """.formatted(
                isOverdue ? "#DC2626" : "#F59E0B",
                invoice.getClient().getName(),
                isOverdue
                        ? "This is a reminder that invoice <strong>#" + invoice.getInvoiceNumber() +
                          "</strong> is now <strong>" + invoice.getDaysOverdue() + " days overdue</strong>."
                        : "This is a friendly reminder that invoice <strong>#" + invoice.getInvoiceNumber() +
                          "</strong> is due on <strong>" + invoice.getDueDate().format(DATE_FORMAT) + "</strong>.",
                invoice.getInvoiceNumber(),
                invoice.getDueDate().format(DATE_FORMAT),
                invoice.getCurrency(),
                invoice.getBalanceDue(),
                businessName
        );
    }

    private String buildWelcomeEmailBody(String fullName) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: #2563EB; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">Welcome to InvoKit! 🎉</h1>
                </div>
                <div style="background: #F9FAFB; padding: 30px; border-radius: 0 0 8px 8px;">
                    <p style="font-size: 16px;">Hi <strong>%s</strong>,</p>
                    <p>Welcome to InvoKit! You're all set to start managing your invoices like a pro.</p>
                    <h3>Here's what you can do:</h3>
                    <ul>
                        <li>✅ Create professional invoices in seconds</li>
                        <li>✅ Track payments and outstanding balances</li>
                        <li>✅ Send automatic payment reminders</li>
                        <li>✅ Generate PDF invoices</li>
                        <li>✅ View revenue reports and analytics</li>
                    </ul>
                    <p>Your free account includes <strong>5 clients</strong> and <strong>20 invoices per month</strong>.</p>
                    <p>Happy invoicing!</p>
                    <p><strong>The InvoKit Team</strong></p>
                </div>
            </body>
            </html>
            """.formatted(fullName);
    }

    // ==========================================
    // HELPER
    // ==========================================
    private String getBusinessName(Invoice invoice) {
        return invoice.getUser().getCompanyName() != null
                ? invoice.getUser().getCompanyName()
                : invoice.getUser().getFullName();
    }
}