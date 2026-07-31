package com.inventory.pos.service;

import com.inventory.pos.dto.ReceiptResponse;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * "Email Bill Distribution" — sends the customer their invoice by email,
 * PDF attached, right after checkout.
 *
 * This is the genuinely FREE-of-cost channel: it reuses the exact same
 * spring-boot-starter-mail + Gmail SMTP setup already wired up for the
 * low-stock alerts in AlertService (see application.properties). Gmail's
 * SMTP relay has no per-email charge at normal shop volumes — unlike SMS or
 * WhatsApp APIs (Twilio etc.), which are metered and cost money once any
 * free trial credit runs out. No new dependency, no paid account, no API key.
 */
@Service
public class BillingEmailService {

    private static final Logger log = LoggerFactory.getLogger(BillingEmailService.class);

    private final JavaMailSender mailSender;
    private final PdfService pdfService;

    @Value("${alerts.email.billing.enabled:true}")
    private boolean billingEmailEnabled;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.business.name:Inventory POS}")
    private String businessName;

    public BillingEmailService(JavaMailSender mailSender, PdfService pdfService) {
        this.mailSender = mailSender;
        this.pdfService = pdfService;
    }

    /**
     * Best-effort: emails the customer their bill with the PDF invoice
     * attached. Returns true only if the mail was actually handed off to
     * SMTP successfully — never throws, so it can never break checkout.
     */
    public boolean emailBill(ReceiptResponse receipt) {
        if (!billingEmailEnabled) {
            return false;
        }
        String to = receipt.getCustomerEmail();
        if (to == null || to.isBlank()) {
            return false;
        }
        if (fromAddress == null || fromAddress.isBlank() || fromAddress.contains("your-alert-email")) {
            log.warn("Bill email skipped: spring.mail.username is still a placeholder in application.properties.");
            return false;
        }

        try {
            byte[] pdf = pdfService.generateInvoicePdf(receipt);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to.trim());
            helper.setFrom(fromAddress);
            helper.setSubject(businessName + " — Invoice for Bill #" + receipt.getBillId());
            helper.setText(buildBody(receipt), false);
            helper.addAttachment("invoice-" + receipt.getBillId() + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdf));

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            // Never let a notification failure roll back or break checkout.
            log.error("Failed to email bill to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    private String buildBody(ReceiptResponse receipt) {
        return String.format(
                "Hi %s,%n%n"
                + "Thank you for shopping with %s!%n%n"
                + "Your bill #%d has been confirmed. Total: Rs. %s%n"
                + "Your invoice is attached to this email as a PDF.%n%n"
                + "We hope to see you again soon.",
                receipt.getCustomerName() == null ? "there" : receipt.getCustomerName(),
                businessName,
                receipt.getBillId(),
                receipt.getTotal().setScale(2, java.math.RoundingMode.HALF_UP));
    }
}
