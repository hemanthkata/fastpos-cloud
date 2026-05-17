package com.fastpos.service;

import com.fastpos.dto.OrderDTO;
import com.fastpos.exception.ResourceNotFoundException;
import com.fastpos.model.*;
import com.fastpos.repository.OrderRepository;
import com.fastpos.repository.ProductRepository;
import com.fastpos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final EmailService emailService;

    @Transactional
    public Order createOrderFromCart(Long userId, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Map<Long, Integer> cartItems = cartService.getRawCart(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .notes(notes)
                .subtotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", entry.getKey()));

            int qty = entry.getValue();
            if (qty > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepository.save(product);

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(qty)
                    .unitPrice(product.getPrice())
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(qty)))
                    .build();

            order.addItem(item);
            subtotal = subtotal.add(item.getSubtotal());
        }

        BigDecimal taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(subtotal.add(taxAmount));

        order = orderRepository.save(order);
        cartService.clearCart(userId); // Clear cart after order creation

        log.info("Order created: {} for user {} | Total: ${}", orderNumber, userId, order.getTotalAmount());

        // Send order confirmation email asynchronously
        emailService.sendOrderConfirmation(user.getEmail(), order);

        return order;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
    }

    public Page<Order> getOrdersByUser(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        order = orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getOrderNumber(), status);
        return order;
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);
        if (order.getStatus() == Order.OrderStatus.PAID || order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel a paid/completed order");
        }

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .customerName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .items(order.getItems().stream().map(item ->
                        OrderDTO.OrderItemDTO.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .sku(item.getProduct().getSku())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build()
                ).collect(Collectors.toList()))
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus().name() : "NONE")
                .paymentMethod(order.getPayment() != null ? order.getPayment().getMethod().name() : "NONE")
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }
}
