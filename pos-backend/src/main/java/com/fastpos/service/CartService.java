package com.fastpos.service;

import com.fastpos.dto.CartDTO;
import com.fastpos.exception.ResourceNotFoundException;
import com.fastpos.model.Product;
import com.fastpos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% tax

    private final ProductRepository productRepository;

    // In-memory cart storage per user (userId -> {productId -> quantity})
    private final Map<Long, Map<Long, Integer>> userCarts = new ConcurrentHashMap<>();

    public CartDTO getCart(Long userId) {
        Map<Long, Integer> cart = userCarts.getOrDefault(userId, new ConcurrentHashMap<>());
        return buildCartDTO(cart);
    }

    public CartDTO addToCart(Long userId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.isActive()) {
            throw new IllegalArgumentException("Product is not available");
        }

        Map<Long, Integer> cart = userCarts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        int currentQty = cart.getOrDefault(productId, 0);
        int newQty = currentQty + quantity;

        if (newQty > product.getStockQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        if (newQty <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, newQty);
        }

        log.debug("Cart updated for user {}: product {} qty {}", userId, productId, newQty);
        return buildCartDTO(cart);
    }

    public CartDTO updateCartItem(Long userId, Long productId, int quantity) {
        Map<Long, Integer> cart = userCarts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            if (quantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQuantity());
            }
            cart.put(productId, quantity);
        }

        return buildCartDTO(cart);
    }

    public CartDTO removeFromCart(Long userId, Long productId) {
        Map<Long, Integer> cart = userCarts.getOrDefault(userId, new ConcurrentHashMap<>());
        cart.remove(productId);
        return buildCartDTO(cart);
    }

    public CartDTO clearCart(Long userId) {
        userCarts.remove(userId);
        return buildCartDTO(new ConcurrentHashMap<>());
    }

    public Map<Long, Integer> getRawCart(Long userId) {
        return userCarts.getOrDefault(userId, new ConcurrentHashMap<>());
    }

    private CartDTO buildCartDTO(Map<Long, Integer> cart) {
        var items = cart.entrySet().stream()
                .map(entry -> {
                    Product product = productRepository.findById(entry.getKey()).orElse(null);
                    if (product == null) return null;

                    BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));

                    return CartDTO.CartItemDTO.builder()
                            .productId(product.getId())
                            .productName(product.getName())
                            .sku(product.getSku())
                            .unitPrice(product.getPrice())
                            .quantity(entry.getValue())
                            .subtotal(subtotal)
                            .imageUrl(product.getImageUrl())
                            .build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartDTO.CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        int totalItems = items.stream().mapToInt(CartDTO.CartItemDTO::getQuantity).sum();

        return CartDTO.builder()
                .items(items)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }
}
