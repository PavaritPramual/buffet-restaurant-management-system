package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.request.CreatePaymentRequest;
import com.buffetrestaurant.dto.response.PaymentResult;

public interface PaymentService {
    

    PaymentResult pay(CreatePaymentRequest request);
    PaymentResult findBySessionId(Long sessionId);
}
