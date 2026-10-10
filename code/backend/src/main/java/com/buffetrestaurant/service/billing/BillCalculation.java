package com.buffetrestaurant.service.billing;

import java.math.BigDecimal;

/** Internal precise result; never serialized as the public API response. */
public record BillCalculation(Long sessionId, BigDecimal subtotalNoneDiscount,
        BigDecimal discountAmount, BigDecimal totalBeforeRounding,
        BigDecimal roundingAdjustment, BigDecimal totalAmount) {
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getSubtotalNoneDiscount() { return subtotalNoneDiscount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalBeforeRounding() { return totalBeforeRounding; }
    public BigDecimal getRoundingAdjustment() { return roundingAdjustment; }
}
