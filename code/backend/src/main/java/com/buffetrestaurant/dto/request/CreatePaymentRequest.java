package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.buffetrestaurant.domain.enums.PaymentMethod;

public record CreatePaymentRequest(
    @NotNull(message = "Session ID is required")
    @Positive (message = "Session ID must be positive")
    Long sessionId,

    @NotNull (message = "Payment Method is required")
    PaymentMethod paymentMethod

) {
}
