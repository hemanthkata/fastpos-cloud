package com.fastpos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private BigDecimal averageOrderValue;
    private Long totalProductsSold;

    private List<DailySales> dailySales;
    private List<TopProduct> topProducts;
    private Map<String, BigDecimal> revenueByCategory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySales {
        private String date;
        private BigDecimal revenue;
        private Long orderCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long quantitySold;
        private BigDecimal revenue;
    }
}
