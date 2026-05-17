package com.fastpos.controller;

import com.fastpos.dto.OrderDTO;
import com.fastpos.model.Order;
import com.fastpos.model.User;
import com.fastpos.repository.UserRepository;
import com.fastpos.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) Map<String, String> request) {
        Long userId = getUserId(userDetails);
        String notes = request != null ? request.getOrDefault("notes", "") : "";
        Order order = orderService.createOrderFromCart(userId, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.toDTO(order));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getUserId(userDetails);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderService.getOrdersByUser(userId, pageRequest);
        return ResponseEntity.ok(orders.map(orderService::toDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.toDTO(orderService.getOrderById(id)));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.toDTO(orderService.getOrderByNumber(orderNumber)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.toDTO(orderService.cancelOrder(id)));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }
}
