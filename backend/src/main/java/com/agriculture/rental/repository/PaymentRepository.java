package com.agriculture.rental.repository;

import com.agriculture.rental.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByBookingUserId(Long userId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'PAID'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'PAID'")
    long countPaidPayments();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paymentStatus = 'PENDING'")
    long countPendingPayments();
}
