package com.agriculture.rental.service;

import com.agriculture.rental.dto.PaymentRequest;
import com.agriculture.rental.model.Booking;
import com.agriculture.rental.model.Payment;
import com.agriculture.rental.repository.BookingRepository;
import com.agriculture.rental.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Create payment for a booking
     */
    @Transactional
    public Payment createPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + request.getBookingId()));

        // Check if payment already exists
        Optional<Payment> existing = paymentRepository.findByBookingId(request.getBookingId());
        if (existing.isPresent() && existing.get().getPaymentStatus() == Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already completed for this booking");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setNotes(request.getNotes());

        // Save farmer's transaction ID if provided
        if (request.getTransactionId() != null && !request.getTransactionId().isEmpty()) {
            payment.setFarmerTransactionId(request.getTransactionId());
        }

        return paymentRepository.save(payment);
    }

    /**
     * Process/confirm payment
     */
    @Transactional
    public Payment processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        if (payment.getPaymentStatus() == Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Payment already processed");
        }

        // Simulate payment processing
        payment.setPaymentStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());

        // Update booking status to ACTIVE
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    /**
     * Refund payment
     */
    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + paymentId));

        if (payment.getPaymentStatus() != Payment.PaymentStatus.PAID) {
            throw new RuntimeException("Only paid payments can be refunded");
        }

        payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);

        // Cancel the booking
        Booking booking = payment.getBooking();
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }

    /**
     * Get all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Get payment by ID
     */
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    /**
     * Get payment by booking ID
     */
    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    /**
     * Get payments by user
     */
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByBookingUserId(userId);
    }

    /**
     * Save payment directly
     */
    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    /**
     * Get total revenue
     */
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = paymentRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Get paid payments count
     */
    public long getPaidCount() {
        return paymentRepository.countPaidPayments();
    }

    /**
     * Get pending payments count
     */
    public long getPendingCount() {
        return paymentRepository.countPendingPayments();
    }
}
