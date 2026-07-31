package com.inventory.pos.service;

import com.inventory.pos.dto.ReceiptResponse;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * "SMS-Based Bill Distribution" from the enhancement doc: after checkout,
 * texts the customer their bill number, total, a thank-you message, and a
 * link to download the PDF invoice. Reuses the same Twilio account as the
 * low-stock alerts (AlertService) — just a different destination number
 * (the customer's, not the shop owner's) and a different message.
 *
 * WhatsApp uses the same Twilio Messages API — just prefix numbers with
 * "whatsapp:" and use a WhatsApp-enabled Twilio sender. Toggle independently
 * via alerts.whatsapp.enabled.
 */
@Service
public class BillingNotificationService {

    @Value("${alerts.sms.billing.enabled:false}")
    private boolean smsBillingEnabled;

    @Value("${alerts.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${twilio.account-sid:}")
    private String twilioSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.from-number:}")
    private String twilioFromNumber;

    @Value("${twilio.whatsapp-from-number:}")
    private String twilioWhatsappFromNumber;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    @Value("${app.business.name:Inventory POS}")
    private String businessName;

    public void notifyCustomer(ReceiptResponse receipt) {
        if ((!smsBillingEnabled && !whatsappEnabled) || receipt.getCustomerPhone() == null || receipt.getCustomerPhone().isBlank()) {
            return;
        }
        if (twilioSid.isBlank() || twilioAuthToken.isBlank()) {
            System.err.println("Bill notification skipped: Twilio credentials not configured.");
            return;
        }

        String invoiceLink = backendBaseUrl + "/api/billing/sales/" + receipt.getBillId() + "/invoice";
        String body = String.format(
                "%s%nBill #%d confirmed. Total: Rs. %s%nThank you for shopping with us!%nDownload your invoice: %s",
                businessName, receipt.getBillId(), receipt.getTotal().setScale(2, java.math.RoundingMode.HALF_UP), invoiceLink);

        String toNumber = receipt.getCustomerPhone().trim();

        if (smsBillingEnabled && !twilioFromNumber.isBlank()) {
            trySend(toNumber, twilioFromNumber, body);
        }
        if (whatsappEnabled && !twilioWhatsappFromNumber.isBlank()) {
            trySend("whatsapp:" + toNumber, "whatsapp:" + twilioWhatsappFromNumber, body);
        }
    }

    private void trySend(String to, String from, String body) {
        try {
            Message.creator(new PhoneNumber(to), new PhoneNumber(from), body).create();
        } catch (Exception e) {
            // Never let a notification failure roll back or break the checkout itself.
            System.err.println("Failed to send bill notification to " + to + ": " + e.getMessage());
        }
    }
}
