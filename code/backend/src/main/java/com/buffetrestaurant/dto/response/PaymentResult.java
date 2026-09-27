package com.buffetrestaurant.dto.response;

import java.time.OffsetDateTime;

import com.buffetrestaurant.domain.enums.PaymentMethod;
import com.buffetrestaurant.domain.enums.PaymentStatus;

public record PaymentResult(
    Long paymentId,
    Long sessionId,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    OffsetDateTime paidAt

){}
