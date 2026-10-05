package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.request.BillingPreviewRequest;
import com.buffetrestaurant.dto.response.BillSummary;
import com.buffetrestaurant.service.billing.BillingPreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/billing")
@Tag(name = "Billing")
public class BillingController {
    private final BillingPreviewService billingPreviewService;

    public BillingController(BillingPreviewService billingPreviewService) {
        this.billingPreviewService = billingPreviewService;
    }

    @PostMapping("/preview")
    @Operation(summary = "Calculate a bill from backend session data",
            description = "Accepts only sessionId. Prices, counts and discounts come from the backend. "
                    + "Does not record a payment or close the session. Requires a logged-in SERVICE_STAFF.")
    @ApiResponse(responseCode = "200", description = "Bill summary")
    @ApiResponse(responseCode = "400", description = "Invalid session ID or session is not ACTIVE")
    @ApiResponse(responseCode = "404", description = "Session not found")
    @ApiResponse(responseCode = "401", description = "Login required")
    @ApiResponse(responseCode = "403", description = "SERVICE_STAFF role required")
    @ApiResponse(responseCode = "503", description = "Billing session provider is not configured")
    public BillSummary preview(@Valid @RequestBody BillingPreviewRequest request) {
        return billingPreviewService.preview(request.sessionId());
    }
}
