package com.inventory.pos.service;

import com.inventory.pos.dto.ReceiptLine;
import com.inventory.pos.dto.ReceiptResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Generates a printable PDF invoice for a receipt — "PDF Invoice and Reports"
 * from the enhancement list. Kept dependency-light (OpenPDF) and self-contained
 * so it works the same whether it's downloaded from the browser or attached/linked
 * from an SMS/WhatsApp bill notification.
 */
@Service
public class PdfService {

    @Value("${app.business.name:Inventory POS}")
    private String businessName;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public byte[] generateInvoicePdf(ReceiptResponse receipt) {
        Document document = new Document(PageSize.A5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(37, 99, 235));
            Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font boldFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

            Paragraph title = new Paragraph(businessName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Invoice / Bill Receipt", labelFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(14);
            document.add(subtitle);

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.addCell(borderless("Bill #: " + receipt.getBillId(), boldFont));
            meta.addCell(borderless(receipt.getBillDate() == null ? "" : receipt.getBillDate().format(DATE_FMT), normalFont));
            meta.addCell(borderless("Customer: " + nullToDash(receipt.getCustomerName()), normalFont));
            meta.addCell(borderless(receipt.getCustomerPhone() == null ? "" : "Phone: " + receipt.getCustomerPhone(), normalFont));
            meta.setSpacingAfter(14);
            document.add(meta);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 1.2f, 1.4f});

            for (String h : new String[]{"Item", "Qty", "Price", "Subtotal"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(37, 99, 235));
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (ReceiptLine line : receipt.getItems()) {
                table.addCell(cell(line.getProductName(), normalFont, Element.ALIGN_LEFT));
                table.addCell(cell(String.valueOf(line.getQuantity()), normalFont, Element.ALIGN_CENTER));
                table.addCell(cell(format(line.getPrice()), normalFont, Element.ALIGN_RIGHT));
                table.addCell(cell(format(line.getSubtotal()), normalFont, Element.ALIGN_RIGHT));
            }
            document.add(table);

            Paragraph total = new Paragraph("Total: Rs. " + format(receipt.getTotal()), titleFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(14);
            document.add(total);

            Paragraph thanks = new Paragraph("Thank you for shopping with us!", normalFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            thanks.setSpacingBefore(24);
            document.add(thanks);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private PdfPCell borderless(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(4);
        return cell;
    }

    private PdfPCell cell(String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(align);
        return cell;
    }

    private String format(java.math.BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }

    private String nullToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }
}
