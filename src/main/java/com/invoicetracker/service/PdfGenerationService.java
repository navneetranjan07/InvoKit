package com.invoicetracker.service;

import com.invoicetracker.model.Invoice;
import com.invoicetracker.model.InvoiceItem;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public byte[] generateInvoicePdf(Invoice invoice) {
        try {
            String html = buildInvoiceHtml(invoice);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(html, outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private String buildInvoiceHtml(Invoice invoice) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="UTF-8"/>
            <style>
                body { font-family: Arial, sans-serif; font-size: 14px; color: #333; margin: 0; padding: 0; }
                .container { max-width: 800px; margin: 0 auto; padding: 40px; }
                .header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 40px; }
                .company-name { font-size: 28px; font-weight: bold; color: #2563EB; }
                .invoice-title { font-size: 32px; font-weight: bold; color: #1F2937; text-align: right; }
                .invoice-number { font-size: 16px; color: #6B7280; text-align: right; }
                .status-badge { display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }
                .status-paid { background: #D1FAE5; color: #065F46; }
                .status-sent { background: #DBEAFE; color: #1E40AF; }
                .status-overdue { background: #FEE2E2; color: #991B1B; }
                .status-draft { background: #F3F4F6; color: #374151; }
                .info-section { display: flex; justify-content: space-between; margin-bottom: 40px; }
                .info-block h4 { font-size: 12px; text-transform: uppercase; color: #6B7280; margin-bottom: 8px; }
                .info-block p { margin: 0; font-size: 14px; }
                .dates-section { display: flex; gap: 40px; margin-bottom: 40px; }
                .date-block h4 { font-size: 12px; text-transform: uppercase; color: #6B7280; margin-bottom: 4px; }
                .date-block p { font-size: 16px; font-weight: bold; }
                table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
                thead tr { background: #1F2937; color: white; }
                thead th { padding: 12px 16px; text-align: left; font-size: 13px; }
                tbody tr { border-bottom: 1px solid #E5E7EB; }
                tbody tr:nth-child(even) { background: #F9FAFB; }
                tbody td { padding: 12px 16px; font-size: 14px; }
                .text-right { text-align: right; }
                .totals-section { margin-left: auto; width: 300px; }
                .totals-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #E5E7EB; }
                .totals-row.total { font-size: 18px; font-weight: bold; color: #1F2937; border-bottom: none; padding-top: 16px; }
                .notes-section { margin-top: 40px; padding-top: 20px; border-top: 1px solid #E5E7EB; }
                .notes-section h4 { font-size: 12px; text-transform: uppercase; color: #6B7280; margin-bottom: 8px; }
                .footer { margin-top: 60px; text-align: center; font-size: 12px; color: #9CA3AF; }
            </style>
            </head>
            <body>
            <div class="container">
            """);

        // Header
        sb.append("<div class='header'>");
        sb.append("<div>");
        sb.append("<div class='company-name'>")
                .append(escapeHtml(invoice.getUser().getCompanyName() != null
                        ? invoice.getUser().getCompanyName()
                        : invoice.getUser().getFullName()))
                .append("</div>");
        sb.append("<p>").append(escapeHtml(invoice.getUser().getEmail())).append("</p>");
        if (invoice.getUser().getPhone() != null) {
            sb.append("<p>").append(escapeHtml(invoice.getUser().getPhone())).append("</p>");
        }
        sb.append("</div>");

        sb.append("<div>");
        sb.append("<div class='invoice-title'>INVOICE</div>");
        sb.append("<div class='invoice-number'>#").append(invoice.getInvoiceNumber()).append("</div>");
        sb.append("<br/>");
        String statusClass = "status-" + invoice.getStatus().name().toLowerCase();
        sb.append("<span class='status-badge ").append(statusClass).append("'>")
                .append(invoice.getStatus().getDisplayName()).append("</span>");
        sb.append("</div>");
        sb.append("</div>");

        // Bill To / From
        sb.append("<div class='info-section'>");

        sb.append("<div class='info-block'>");
        sb.append("<h4>Bill To</h4>");
        sb.append("<p><strong>").append(escapeHtml(invoice.getClient().getName())).append("</strong></p>");
        if (invoice.getClient().getCompanyName() != null) {
            sb.append("<p>").append(escapeHtml(invoice.getClient().getCompanyName())).append("</p>");
        }
        if (invoice.getClient().getEmail() != null) {
            sb.append("<p>").append(escapeHtml(invoice.getClient().getEmail())).append("</p>");
        }
        if (invoice.getClient().getAddress() != null) {
            sb.append("<p>").append(escapeHtml(invoice.getClient().getAddress())).append("</p>");
        }
        sb.append("</div>");

        // Dates
        sb.append("<div>");
        sb.append("<div class='dates-section'>");

        sb.append("<div class='date-block'>");
        sb.append("<h4>Issue Date</h4>");
        sb.append("<p>").append(invoice.getIssueDate().format(DATE_FORMAT)).append("</p>");
        sb.append("</div>");

        sb.append("<div class='date-block'>");
        sb.append("<h4>Due Date</h4>");
        sb.append("<p>").append(invoice.getDueDate().format(DATE_FORMAT)).append("</p>");
        sb.append("</div>");

        sb.append("</div>");
        sb.append("</div>");

        sb.append("</div>");

        // Line Items Table
        sb.append("<table>");
        sb.append("<thead><tr>");
        sb.append("<th>#</th>");
        sb.append("<th>Description</th>");
        sb.append("<th class='text-right'>Qty</th>");
        sb.append("<th class='text-right'>Unit Price</th>");
        sb.append("<th class='text-right'>Amount</th>");
        sb.append("</tr></thead>");
        sb.append("<tbody>");

        int i = 1;
        for (InvoiceItem item : invoice.getItems()) {
            sb.append("<tr>");
            sb.append("<td>").append(i++).append("</td>");
            sb.append("<td>").append(escapeHtml(item.getDescription())).append("</td>");
            sb.append("<td class='text-right'>").append(item.getQuantity()).append("</td>");
            sb.append("<td class='text-right'>")
                    .append(invoice.getCurrency()).append(" ")
                    .append(formatAmount(item.getUnitPrice()))
                    .append("</td>");
            sb.append("<td class='text-right'>")
                    .append(invoice.getCurrency()).append(" ")
                    .append(formatAmount(item.getAmount()))
                    .append("</td>");
            sb.append("</tr>");
        }

        sb.append("</tbody></table>");

        // Totals
        sb.append("<div class='totals-section'>");

        sb.append("<div class='totals-row'>");
        sb.append("<span>Subtotal</span>");
        sb.append("<span>").append(invoice.getCurrency()).append(" ")
                .append(formatAmount(invoice.getSubtotal())).append("</span>");
        sb.append("</div>");

        if (invoice.getTaxRate() != null && invoice.getTaxRate().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append("<div class='totals-row'>");
            sb.append("<span>Tax (").append(invoice.getTaxRate()).append("%)</span>");
            sb.append("<span>").append(invoice.getCurrency()).append(" ")
                    .append(formatAmount(invoice.getTaxAmount())).append("</span>");
            sb.append("</div>");
        }

        if (invoice.getDiscountAmount() != null && invoice.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append("<div class='totals-row'>");
            sb.append("<span>Discount</span>");
            sb.append("<span>-").append(invoice.getCurrency()).append(" ")
                    .append(formatAmount(invoice.getDiscountAmount())).append("</span>");
            sb.append("</div>");
        }

        sb.append("<div class='totals-row total'>");
        sb.append("<span>Total</span>");
        sb.append("<span>").append(invoice.getCurrency()).append(" ")
                .append(formatAmount(invoice.getTotalAmount())).append("</span>");
        sb.append("</div>");

        if (invoice.getAmountPaid() != null &&
                invoice.getAmountPaid().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append("<div class='totals-row'>");
            sb.append("<span>Amount Paid</span>");
            sb.append("<span>-").append(invoice.getCurrency()).append(" ")
                    .append(formatAmount(invoice.getAmountPaid())).append("</span>");
            sb.append("</div>");

            sb.append("<div class='totals-row total'>");
            sb.append("<span>Balance Due</span>");
            sb.append("<span>").append(invoice.getCurrency()).append(" ")
                    .append(formatAmount(invoice.getBalanceDue())).append("</span>");
            sb.append("</div>");
        }

        sb.append("</div>");

        // Notes
        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            sb.append("<div class='notes-section'>");
            sb.append("<h4>Notes</h4>");
            sb.append("<p>").append(escapeHtml(invoice.getNotes())).append("</p>");
            sb.append("</div>");
        }

        // Terms
        if (invoice.getTerms() != null && !invoice.getTerms().isEmpty()) {
            sb.append("<div class='notes-section'>");
            sb.append("<h4>Terms & Conditions</h4>");
            sb.append("<p>").append(escapeHtml(invoice.getTerms())).append("</p>");
            sb.append("</div>");
        }

        // Footer
        sb.append("<div class='footer'>");
        sb.append("<p>Generated by InvoKit • Thank you for your business!</p>");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}