package com.buffetrestaurant.dto.response;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import com.buffetrestaurant.domain.enums.CustomerBillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
public record CustomerBillStatusResponse(Long sessionId,
        @Schema(description="Bill flow: NOT_REQUESTED allows orders; REQUESTED stops new orders; PAID means payment recorded, session remains ACTIVE until staff closes it.") CustomerBillStatus status,
        OffsetDateTime requestedAt,
        @Schema(description="Bill totals. totalAmount always means the net bill total, never the outstanding balance.") BillSummary bill,
        @Schema(description="Outstanding amount; zero after PAID.") BigDecimal dueAmount,
        @Schema(description="Recorded PAID amount; zero before payment.") BigDecimal paidAmount) {}
