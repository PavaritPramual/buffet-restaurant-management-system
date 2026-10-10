package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import com.buffetrestaurant.domain.enums.PaymentMethod;

public record CreatePaymentRequest(
    @NotNull(message = "Session ID is required")
    @Positive (message = "Session ID must be positive")
    @Schema(description = "Dining session to charge; amount is resolved by the backend", example = "12")
    Long sessionId,

    @NotNull (message = "Payment Method is required")
    @Schema(description = "Staff-confirmed payment method", example = "CASH")
    PaymentMethod paymentMethod

) {
}
