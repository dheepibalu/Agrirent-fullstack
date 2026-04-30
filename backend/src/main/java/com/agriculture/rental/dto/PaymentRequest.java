package com.agriculture.rental.dto;

import com.agriculture.rental.model.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private Payment.PaymentMethod paymentMethod;

    private String transactionId;  // Farmer's UPI transaction ID

    private String notes;
}
