package com.agriculture.rental.controller;

import com.agriculture.rental.dto.ApiResponse;
import com.agriculture.rental.dto.PaymentRequest;
import com.agriculture.rental.model.Payment;
import com.agriculture.rental.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * POST /api/payments - Create a new payment
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            Payment payment = paymentService.createPayment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payment created successfully", payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/payments/{id}/process - Process/confirm payment
     */
    @PutMapping("/{id}/process")
    public ResponseEntity<ApiResponse<Payment>> processPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.processPayment(id);
            return ResponseEntity.ok(ApiResponse.success("Payment processed successfully! Transaction ID: "
                    + payment.getTransactionId(), payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * PUT /api/payments/{id}/refund - Refund payment
     */
    @PutMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Payment>> refundPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.refundPayment(id);
            return ResponseEntity.ok(ApiResponse.success("Payment refunded successfully", payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/payments - Get all payments
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments(
            @RequestParam(required = false) Long userId) {
        List<Payment> payments;
        if (userId != null) {
            payments = paymentService.getPaymentsByUser(userId);
        } else {
            payments = paymentService.getAllPayments();
        }
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", payments));
    }

    /**
     * GET /api/payments/{id} - Get payment by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(p -> ResponseEntity.ok(ApiResponse.success("Payment found", p)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Payment not found")));
    }

    /**
     * GET /api/payments/booking/{bookingId} - Get payment by booking
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByBooking(@PathVariable Long bookingId) {
        Optional<Payment> payment = paymentService.getPaymentByBookingId(bookingId);
        return payment.map(p -> ResponseEntity.ok(ApiResponse.success("Payment found", p)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("No payment found for this booking")));
    }

    /**
     * PUT /api/payments/{id}/submit - Farmer submits transaction ID
     */
    @PutMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Payment>> submitTransactionId(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        try {
            Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Payment not found"));
            }
            Payment payment = paymentOpt.get();
            String txnId = body.get("transactionId");
            String method = body.get("paymentMethod");

            if (txnId != null && !txnId.isEmpty()) {
                payment.setFarmerTransactionId(txnId);
            }
            if (method != null && !method.isEmpty()) {
                try {
                    payment.setPaymentMethod(Payment.PaymentMethod.valueOf(method));
                } catch (Exception e) {
                    payment.setPaymentMethod(Payment.PaymentMethod.UPI);
                }
            }
            // Save updated payment
            payment = paymentService.savePayment(payment);
            return ResponseEntity.ok(ApiResponse.success("Transaction ID submitted. Awaiting admin verification.", payment));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * GET /api/payments/summary - Get payment summary stats
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", paymentService.getTotalRevenue());
        summary.put("paidCount", paymentService.getPaidCount());
        summary.put("pendingCount", paymentService.getPendingCount());
        return ResponseEntity.ok(ApiResponse.success("Payment summary retrieved", summary));
    }
}
