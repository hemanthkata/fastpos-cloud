package com.fastpos.service;

import com.fastpos.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("emailExecutor")
    public void sendOrderConfirmation(String toEmail, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - " + order.getOrderNumber());
            message.setText(buildOrderConfirmationBody(order));
            mailSender.send(message);
            log.info("Order confirmation email sent to {} for order {}", toEmail, order.getOrderNumber());
        } catch (Exception e) {
            log.warn("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async("emailExecutor")
    public void sendPaymentReceipt(String toEmail, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Payment Receipt - " + order.getOrderNumber());
            message.setText("Payment of $" + order.getTotalAmount() + " received for order " + order.getOrderNumber());
            mailSender.send(message);
            log.info("Payment receipt email sent to {}", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send payment receipt email: {}", e.getMessage());
        }
    }

    private String buildOrderConfirmationBody(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(order.getUser().getFirstName()).append(",\n\n");
        sb.append("Your order has been placed successfully!\n\n");
        sb.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        sb.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
        sb.append("Status: ").append(order.getStatus()).append("\n\n");
        sb.append("Items:\n");
        order.getItems().forEach(item ->
                sb.append("  - ").append(item.getProduct().getName())
                        .append(" x").append(item.getQuantity())
                        .append(" = $").append(item.getSubtotal()).append("\n"));
        sb.append("\nThank you for shopping with FastPOS!");
        return sb.toString();
    }
}
