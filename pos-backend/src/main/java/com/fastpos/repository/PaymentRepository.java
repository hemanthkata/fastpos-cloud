package com.fastpos.repository;

import com.fastpos.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
    Optional<Payment> findByOrderId(Long orderId);
}
