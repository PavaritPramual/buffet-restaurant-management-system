package com.buffetrestaurant.dto.response;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

/** Stable display breakdown; precise intermediate calculations stay inside BillingEngine. */
public record BillSummary(
        @Schema(example = "12") Long sessionId,
        @Schema(description = "Subtotal in the currency's major unit; JSON number", type = "number",
                format = "double", implementation = Double.class, example = "997.50")
        BigDecimal subtotalAmount,
        @Schema(description = "Discount in the currency's major unit; JSON number", type = "number",
                format = "double", implementation = Double.class, example = "0.00")
        BigDecimal discountAmount,
        @Schema(description = "Net bill total, not the outstanding balance; JSON number", type = "number",
                format = "double", implementation = Double.class, example = "997.50")
        BigDecimal totalAmount
) {
}
