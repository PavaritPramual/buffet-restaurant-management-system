package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.Payment;
import com.buffetrestaurant.integration.payment.PaymentStatusLookup;
import com.buffetrestaurant.repository.PaymentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@ConditionalOnProperty(
        name = "app.payment.status-provider",
        havingValue = "database"
)
public class DatabasePaymentStatusLookup implements PaymentStatusLookup {

    private final PaymentRepository paymentRepository;

    public DatabasePaymentStatusLookup(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public PaymentVerification findPaymentForSession(Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("A positive session ID is required");
        }

        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElse(null);

        if (payment == null) {
            return null;
        }

        return new PaymentVerification(
                payment.getSessionId(),
                payment.getPaymentStatus()
        );
    }
}