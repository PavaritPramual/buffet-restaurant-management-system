package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.CreatePaymentRequest;
import com.buffetrestaurant.dto.response.PaymentResult;
import com.buffetrestaurant.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Record a staff-confirmed payment")
    @PostMapping
    public ResponseEntity<PaymentResult> pay(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        PaymentResult result = paymentService.pay(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}