package com.buffetrestaurant.dto.response;

import java.time.OffsetDateTime;

import com.buffetrestaurant.domain.enums.PaymentMethod;
import com.buffetrestaurant.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentResult(
    @Schema(example = "81")
    Long paymentId,
    @Schema(example = "12")
    Long sessionId,
    @Schema(description = "Recorded amount as a JSON number", type = "number", format = "double",
            implementation = Double.class, example = "997.50")
    java.math.BigDecimal amount,
    @Schema(example = "CASH")
    PaymentMethod paymentMethod,
    @Schema(example = "PAID")
    PaymentStatus paymentStatus,
    @Schema(description = "ISO-8601 timestamp in UTC", example = "2026-10-07T08:09:10Z")
    OffsetDateTime paidAt

){}
