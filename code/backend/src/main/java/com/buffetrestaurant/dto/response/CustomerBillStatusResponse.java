package com.buffetrestaurant.dto.response;
import java.time.OffsetDateTime;
public record CustomerBillStatusResponse(Long sessionId,
        @io.swagger.v3.oas.annotations.media.Schema(allowableValues={"NOT_REQUESTED", "REQUESTED", "PAID"}) String status,
        OffsetDateTime requestedAt, BillSummary bill) {}
