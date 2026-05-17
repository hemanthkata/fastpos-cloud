package com.fastpos.service;

import com.fastpos.dto.AnalyticsDTO;
import com.fastpos.model.Order;
import com.fastpos.model.OrderItem;
import com.fastpos.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final OrderRepository orderRepository;

    public AnalyticsDTO getSalesAnalytics(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Order> paidOrders = orderRepository.findPaidOrdersByDateRange(fromDateTime, toDateTime);

        BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = paidOrders.size();

        BigDecimal avgOrderValue = totalOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long totalProductsSold = paidOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();

        List<AnalyticsDTO.DailySales> dailySales = buildDailySales(paidOrders, from, to);
        List<AnalyticsDTO.TopProduct> topProducts = buildTopProducts(paidOrders);
        Map<String, BigDecimal> revenueByCategory = buildRevenueByCategory(paidOrders);

        return AnalyticsDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(avgOrderValue)
                .totalProductsSold(totalProductsSold)
                .dailySales(dailySales)
                .topProducts(topProducts)
                .revenueByCategory(revenueByCategory)
                .build();
    }

    public String exportToCsv(LocalDate from, LocalDate to) {
        LocalDateTime fromDT = from.atStartOfDay();
        LocalDateTime toDT = to.atTime(LocalTime.MAX);
        List<Order> orders = orderRepository.findPaidOrdersByDateRange(fromDT, toDT);

        StringBuilder csv = new StringBuilder();
        csv.append("Order Number,Date,Customer,Items,Subtotal,Tax,Total,Status\n");

        for (Order order : orders) {
            String items = order.getItems().stream()
                    .map(i -> i.getProduct().getName() + " x" + i.getQuantity())
                    .collect(Collectors.joining("; "));

            csv.append(String.format("%s,%s,%s %s,\"%s\",%s,%s,%s,%s\n",
                    order.getOrderNumber(),
                    order.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    order.getUser().getFirstName(), order.getUser().getLastName(),
                    items,
                    order.getSubtotal(), order.getTaxAmount(), order.getTotalAmount(),
                    order.getStatus()));
        }
        return csv.toString();
    }

    private List<AnalyticsDTO.DailySales> buildDailySales(List<Order> orders, LocalDate from, LocalDate to) {
        Map<LocalDate, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<AnalyticsDTO.DailySales> result = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            List<Order> dayOrders = grouped.getOrDefault(current, Collections.emptyList());
            BigDecimal revenue = dayOrders.stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(AnalyticsDTO.DailySales.builder()
                    .date(current.toString())
                    .revenue(revenue)
                    .orderCount((long) dayOrders.size())
                    .build());
            current = current.plusDays(1);
        }
        return result;
    }

    private List<AnalyticsDTO.TopProduct> buildTopProducts(List<Order> orders) {
        Map<Long, AnalyticsDTO.TopProduct> productMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                Long pid = item.getProduct().getId();
                AnalyticsDTO.TopProduct existing = productMap.get(pid);
                if (existing == null) {
                    productMap.put(pid, AnalyticsDTO.TopProduct.builder()
                            .productId(pid)
                            .productName(item.getProduct().getName())
                            .quantitySold((long) item.getQuantity())
                            .revenue(item.getSubtotal())
                            .build());
                } else {
                    existing.setQuantitySold(existing.getQuantitySold() + item.getQuantity());
                    existing.setRevenue(existing.getRevenue().add(item.getSubtotal()));
                }
            }
        }

        return productMap.values().stream()
                .sorted(Comparator.comparing(AnalyticsDTO.TopProduct::getRevenue).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    private Map<String, BigDecimal> buildRevenueByCategory(List<Order> orders) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                String category = item.getProduct().getCategory() != null
                        ? item.getProduct().getCategory().getName() : "Uncategorized";
                result.merge(category, item.getSubtotal(), BigDecimal::add);
            }
        }
        return result;
    }
}
