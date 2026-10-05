package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import java.math.BigDecimal;

/** Stateless percentage policy; percentages come only from backend-owned context. */
public class PromotionDiscountStrategy implements DiscountCalculationStrategy {
    @Override
    public BigDecimal calculateDiscount(BillingContext context, BigDecimal subtotal) {
        if (context.getDiscountContext() == null) return BigDecimal.ZERO;
        Object value = context.getDiscountContext().get("percentage");
        if (!(value instanceof Number)) throw new IllegalArgumentException("Percentage must be a number");
        BigDecimal percentage;
        try { percentage = new BigDecimal(value.toString()); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Percentage must be finite", exception); }
        if (percentage.signum() < 0 || percentage.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        return subtotal.multiply(percentage).divide(new BigDecimal("100"));
    }
}
