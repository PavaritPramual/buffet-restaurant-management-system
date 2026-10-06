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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/sessions/{sessionId}")
    @Operation(summary = "Read the recorded payment for a session",
            description = "Requires a logged-in SERVICE_STAFF. Reading does not create or retry a payment.")
    @ApiResponse(responseCode = "200", description = "Recorded payment")
    @ApiResponse(responseCode = "400", description = "Invalid session ID")
    @ApiResponse(responseCode = "401", description = "Login required")
    @ApiResponse(responseCode = "403", description = "SERVICE_STAFF role required")
    @ApiResponse(responseCode = "404", description = "No recorded payment for this session")
    public PaymentResult findBySessionId(@PathVariable Long sessionId) {
        return paymentService.findBySessionId(sessionId);
    }

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Record a staff-confirmed payment",
            description = "Requires ACTIVE session with a bill request. Locks the session with Order, Bill Request and Close before re-checking and recording payment.")
    @ApiResponse(responseCode = "201", description = "Payment recorded as PAID")
    @ApiResponse(responseCode = "400", description = "Invalid payment or inactive session")
    @ApiResponse(responseCode = "401", description = "Login required")
    @ApiResponse(responseCode = "403", description = "SERVICE_STAFF role required")
    @ApiResponse(responseCode = "404", description = "Session not found")
    @ApiResponse(responseCode = "409", description = "Bill not requested, payment already recorded or data conflict")
    @ApiResponse(responseCode = "503", description = "Billing provider unavailable")
    @PostMapping
    public ResponseEntity<PaymentResult> pay(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        PaymentResult result = paymentService.pay(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
