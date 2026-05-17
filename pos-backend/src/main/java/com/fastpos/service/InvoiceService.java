package com.fastpos.service;

import com.fastpos.model.Order;
import com.fastpos.model.OrderItem;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class InvoiceService {

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(79, 70, 229);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(243, 244, 246);

    public byte[] generateInvoicePdf(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Company Header
            document.add(new Paragraph("FastPOS")
                    .setFontSize(28).setBold()
                    .setFontColor(PRIMARY_COLOR)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Cloud Point-of-Sale System")
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Invoice Info
            document.add(new Paragraph("INVOICE")
                    .setFontSize(20).setBold()
                    .setFontColor(PRIMARY_COLOR));

            document.add(new Paragraph("Order #: " + order.getOrderNumber())
                    .setFontSize(11));
            document.add(new Paragraph("Date: " + order.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy hh:mm a")))
                    .setFontSize(11));
            document.add(new Paragraph("Customer: " + order.getUser().getFirstName() + " " + order.getUser().getLastName())
                    .setFontSize(11));
            document.add(new Paragraph("Email: " + order.getUser().getEmail())
                    .setFontSize(11).setMarginBottom(20));

            // Items Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 6, 2, 3, 3}))
                    .useAllAvailableWidth();

            // Header
            String[] headers = {"SKU", "Product", "Qty", "Unit Price", "Subtotal"};
            for (String header : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setBold().setFontSize(10))
                        .setBackgroundColor(HEADER_BG)
                        .setPadding(8));
            }

            // Rows
            for (OrderItem item : order.getItems()) {
                table.addCell(new Cell().add(new Paragraph(item.getProduct().getSku()).setFontSize(9)).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(item.getProduct().getName()).setFontSize(9)).setPadding(6));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(item.getQuantity())).setFontSize(9)).setPadding(6));
                table.addCell(new Cell().add(new Paragraph("$" + item.getUnitPrice()).setFontSize(9)).setPadding(6));
                table.addCell(new Cell().add(new Paragraph("$" + item.getSubtotal()).setFontSize(9)).setPadding(6));
            }

            document.add(table);

            // Totals
            document.add(new Paragraph("\n"));

            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{7, 3}))
                    .useAllAvailableWidth();

            totalsTable.addCell(new Cell().add(new Paragraph("Subtotal:").setBold().setFontSize(11))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(null));
            totalsTable.addCell(new Cell().add(new Paragraph("$" + order.getSubtotal()).setFontSize(11))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(null));

            totalsTable.addCell(new Cell().add(new Paragraph("Tax (8%):").setBold().setFontSize(11))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(null));
            totalsTable.addCell(new Cell().add(new Paragraph("$" + order.getTaxAmount()).setFontSize(11))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(null));

            totalsTable.addCell(new Cell().add(new Paragraph("TOTAL:").setBold().setFontSize(14).setFontColor(PRIMARY_COLOR))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(null));
            totalsTable.addCell(new Cell().add(new Paragraph("$" + order.getTotalAmount()).setBold().setFontSize(14).setFontColor(PRIMARY_COLOR))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(null));

            document.add(totalsTable);

            // Footer
            document.add(new Paragraph("\n\nThank you for your purchase!")
                    .setFontSize(12).setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(PRIMARY_COLOR));

            document.add(new Paragraph("FastPOS — Powered by Spring Boot")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            log.info("Invoice PDF generated for order {}", order.getOrderNumber());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF for order {}", order.getOrderNumber(), e);
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }
}
