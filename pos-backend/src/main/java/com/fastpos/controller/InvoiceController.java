package com.fastpos.controller;

import com.fastpos.model.Order;
import com.fastpos.service.InvoiceService;
import com.fastpos.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final OrderService orderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        byte[] pdfBytes = invoiceService.generateInvoicePdf(order);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + order.getOrderNumber() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}
