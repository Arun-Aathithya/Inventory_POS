package com.inventory.pos.service;

import com.inventory.pos.model.Product;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final JavaMailSender mailSender;

    @Value("${alerts.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${alerts.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${alerts.email.to}")
    private String emailTo;

    @Value("${alerts.sms.to:}")
    private String smsTo;

    @Value("${twilio.account-sid:}")
    private String twilioSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    public AlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void init() {
        if (smsEnabled && !twilioSid.isBlank() && !twilioAuthToken.isBlank()) {
            Twilio.init(twilioSid, twilioAuthToken);
        }
        if (emailEnabled) {
            if (emailTo == null || emailTo.isBlank() || emailTo.contains("example.com")) {
                log.warn("*** EMAIL ALERTS ARE ENABLED BUT alerts.email.to LOOKS UNCONFIGURED ({}). "
                        + "Alerts will 'succeed' at the code level but never actually be delivered until you set a real address. ***", emailTo);
            }
        }
    }

    // Fires both channels (whichever are enabled) for the given low-stock products.
    // Returns per-channel success so callers (e.g. the REST controller) can report
    // the TRUE outcome instead of assuming success just because it was attempted.
    public AlertResult sendLowStockAlert(List<Product> lowStockProducts) {
        if (lowStockProducts == null || lowStockProducts.isEmpty()) {
            return new AlertResult(false, null, false, null);
        }

        String summary = buildSummary(lowStockProducts);

        boolean emailOk = false;
        String emailError = null;
        boolean smsOk = false;
        String smsError = null;

        if (emailEnabled) {
            try {
                sendEmail(summary, lowStockProducts.size());
                emailOk = true;
            } catch (Exception e) {
                emailError = rootCauseMessage(e);
                log.error("Low-stock EMAIL alert failed to send to [{}]: {}", emailTo, emailError, e);
            }
        }
        if (smsEnabled) {
            try {
                sendSms(summary, lowStockProducts.size());
                smsOk = true;
            } catch (Exception e) {
                smsError = rootCauseMessage(e);
                log.error("Low-stock SMS alert failed to send: {}", smsError, e);
            }
        }
        return new AlertResult(emailEnabled, emailOk ? null : emailError, smsEnabled, smsOk ? null : smsError);
    }

    /** Sends a one-off test email so SMTP config can be verified independent of stock data. */
    public void sendTestEmail() {
        sendEmail("This is a test email from the Inventory POS system to confirm SMTP is configured correctly.", 0);
    }

    private String rootCauseMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() != null ? cur.getMessage() : cur.toString();
    }

    /** Result of an alert attempt — lets the API report what ACTUALLY happened, not just "we tried". */
    public record AlertResult(boolean emailAttempted, String emailError, boolean smsAttempted, String smsError) {
        public boolean emailSucceeded() { return emailAttempted && emailError == null; }
        public boolean smsSucceeded() { return smsAttempted && smsError == null; }
    }

    private String buildSummary(List<Product> products) {
        return products.stream()
                .map(p -> String.format("- %s (stock: %d, threshold: %d)",
                        p.getName(), p.getStock(), p.getLowstock()))
                .collect(Collectors.joining("\n"));
    }

    // NOTE: intentionally does NOT catch exceptions here — callers (sendLowStockAlert,
    // sendTestEmail) need the real failure to surface instead of being silently dropped,
    // which was the root cause of alerts "succeeding" while no mail ever arrived.
    private void sendEmail(String summary, int count) {
        if (emailTo == null || emailTo.isBlank() || emailTo.contains("example.com")) {
            throw new IllegalStateException(
                    "alerts.email.to is not configured (still a placeholder: '" + emailTo + "'). "
                    + "Set a real recipient address in application.properties.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailTo.split(","));
        message.setSubject(count > 0 ? "[Inventory Alert] " + count + " product(s) low on stock" : "[Inventory POS] Test email");
        message.setText(count > 0
                ? "The following products are at or below their low-stock threshold:\n\n" + summary + "\n\nPlease reorder soon."
                : summary);
        mailSender.send(message);
    }

    private void sendSms(String summary, int count) {
        if (twilioSid.isBlank() || twilioFromNumber.isBlank() || smsTo.isBlank()) {
            throw new IllegalStateException("SMS alert skipped: Twilio credentials/numbers not configured.");
        }
        String body = count + " product(s) low on stock:\n" + summary;
        // SMS has a length limit; trim if needed so Twilio doesn't reject/split excessively
        if (body.length() > 1500) {
            body = body.substring(0, 1490) + "...";
        }
        Exception lastError = null;
        for (String toNumber : smsTo.split(",")) {
            try {
                Message.creator(
                        new PhoneNumber(toNumber.trim()),
                        new PhoneNumber(twilioFromNumber),
                        body
                ).create();
            } catch (Exception e) {
                lastError = e;
                log.error("Failed to send low-stock SMS to {}: {}", toNumber, e.getMessage(), e);
            }
        }
        if (lastError != null) {
            throw new RuntimeException("One or more SMS sends failed: " + lastError.getMessage(), lastError);
        }
    }
}
