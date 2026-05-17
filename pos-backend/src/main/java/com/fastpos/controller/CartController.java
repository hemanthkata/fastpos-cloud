package com.fastpos.controller;

import com.fastpos.dto.CartDTO;
import com.fastpos.model.User;
import com.fastpos.repository.UserRepository;
import com.fastpos.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTO> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(userDetails);
        Long productId = Long.valueOf(request.get("productId").toString());
        int quantity = Integer.parseInt(request.getOrDefault("quantity", 1).toString());
        return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
    }

    @PutMapping("/update")
    public ResponseEntity<CartDTO> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> request) {
        Long userId = getUserId(userDetails);
        Long productId = Long.valueOf(request.get("productId").toString());
        int quantity = Integer.parseInt(request.get("quantity").toString());
        return ResponseEntity.ok(cartService.updateCartItem(userId, productId, quantity));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartDTO> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.removeFromCart(userId, productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    private Long getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getId();
    }
}
