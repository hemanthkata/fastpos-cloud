package com.fastpos.controller;

import com.fastpos.model.Payment;
import com.fastpos.service.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Long> request) throws StripeException {
        Long orderId = request.get("orderId");
        Payment payment = paymentService.createPaymentIntent(orderId);
        return ResponseEntity.ok(Map.of(
                "clientSecret", payment.getStripeClientSecret(),
                "paymentIntentId", payment.getStripePaymentIntentId()
        ));
    }

    @PostMapping("/cash")
    public ResponseEntity<Map<String, String>> processCashPayment(@RequestBody Map<String, Long> request) {
        Long orderId = request.get("orderId");
        Payment payment = paymentService.processCashPayment(orderId);
        return ResponseEntity.ok(Map.of(
                "status", payment.getStatus().name(),
                "method", payment.getMethod().name()
        ));
    }
}
