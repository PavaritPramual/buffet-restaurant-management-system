package com.buffetrestaurant.dto.response;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
import com.buffetrestaurant.domain.enums.CustomerBillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
public record CustomerBillStatusResponse(
        @Schema(example = "12") Long sessionId,
        @Schema(description = "Bill flow state, distinct from dining-session and payment states", example = "PAID")
        CustomerBillStatus status,
        @Schema(description = "ISO-8601 UTC timestamp; null until a bill is requested", example = "2026-10-07T08:00:00Z",
                nullable = true)
        OffsetDateTime requestedAt,
        @Schema(description = "Bill totals. totalAmount always means the net bill total, never the outstanding balance.")
        BillSummary bill,
        @Schema(description = "Outstanding amount as a JSON number; zero after PAID", type = "number",
                format = "double", implementation = Double.class, example = "0.00")
        BigDecimal dueAmount,
        @Schema(description = "Recorded PAID amount as a JSON number; zero before payment", type = "number",
                format = "double", implementation = Double.class, example = "997.50")
        BigDecimal paidAmount
) {
}
