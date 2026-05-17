package com.fastpos.service;

import com.fastpos.model.Order;
import com.fastpos.model.Payment;
import com.fastpos.repository.OrderRepository;
import com.fastpos.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment createPaymentIntent(Long orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (order.getPayment() != null && order.getPayment().getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            throw new IllegalArgumentException("Order is already paid");
        }

        long amountInCents = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .setDescription("FastPOS Order: " + order.getOrderNumber())
                .putMetadata("orderId", order.getId().toString())
                .putMetadata("orderNumber", order.getOrderNumber())
                .addPaymentMethodType("card")
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(Payment.PaymentMethod.STRIPE)
                .status(Payment.PaymentStatus.PENDING)
                .stripePaymentIntentId(paymentIntent.getId())
                .stripeClientSecret(paymentIntent.getClientSecret())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment intent created for order {}: {}", order.getOrderNumber(), paymentIntent.getId());
        return payment;
    }

    @Transactional
    public Payment processCashPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(Payment.PaymentMethod.CASH)
                .status(Payment.PaymentStatus.SUCCEEDED)
                .build();

        payment = paymentRepository.save(payment);
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Cash payment processed for order {}", order.getOrderNumber());
        return payment;
    }

    @Transactional
    public void handleStripeWebhook(String paymentIntentId, String status) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for intent: " + paymentIntentId));

        Order order = payment.getOrder();
        switch (status) {
            case "succeeded":
                payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
                order.setStatus(Order.OrderStatus.PAID);
                break;
            case "payment_failed":
                payment.setStatus(Payment.PaymentStatus.FAILED);
                break;
            default:
                payment.setStatus(Payment.PaymentStatus.PROCESSING);
        }
        paymentRepository.save(payment);
        orderRepository.save(order);
    }
}
