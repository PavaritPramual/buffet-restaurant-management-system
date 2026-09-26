package com.buffetrestaurant.integration.payment;

import com.buffetrestaurant.domain.enums.PaymentStatus;

/** A narrow adapter contract; the Payment module owns its entity and result DTO. */
public interface PaymentStatusLookup {
    PaymentVerification findPaymentForSession(Long sessionId);

    record PaymentVerification(Long sessionId, PaymentStatus status) {
    }
}
