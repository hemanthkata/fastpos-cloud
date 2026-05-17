package com.fastpos.repository;

import com.fastpos.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :from AND :to")
    List<Order> findByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT o FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
    List<Order> findPaidOrdersByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
    Long countPaidOrdersByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'PAID' AND o.createdAt BETWEEN :from AND :to")
    java.math.BigDecimal sumRevenueByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
