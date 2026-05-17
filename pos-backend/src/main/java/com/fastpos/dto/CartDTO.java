package com.fastpos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private int totalItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDTO {
        private Long productId;
        private String productName;
        private String sku;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subtotal;
        private String imageUrl;
    }
}
