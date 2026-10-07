package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Orchestrates interchangeable pricing/discount policies; keeps intermediate math internal. */
public class BillingEngine implements BillCalculator {
    private final BillCalculationStrategy pricing;
    private final DiscountCalculationStrategy discount;

    public BillingEngine(BillCalculationStrategy pricing) {
        this(pricing, new PromotionDiscountStrategy());
    }
    public BillingEngine(BillCalculationStrategy pricing, DiscountCalculationStrategy discount) {
        if (pricing == null || discount == null) throw new IllegalArgumentException("Calculation strategies required");
        this.pricing = pricing;
        this.discount = discount;
    }
    public BillCalculation calculate(BillingContext context) {
        if (context == null || context.getSessionId() == null || context.getSessionId() <= 0)
            throw new IllegalArgumentException("A positive session ID is required");
        BigDecimal subtotal = pricing.calculate(context);
        BigDecimal reduction = discount.calculateDiscount(context, subtotal);
        if (subtotal == null || reduction == null || subtotal.signum() < 0 || reduction.signum() < 0
                || reduction.compareTo(subtotal) > 0) throw new IllegalArgumentException("Invalid strategy result");
        BigDecimal preciseTotal = subtotal.subtract(reduction);
        BigDecimal total = preciseTotal.setScale(2, RoundingMode.HALF_UP);
        return new BillCalculation(context.getSessionId(), subtotal, reduction,
                preciseTotal, total.subtract(preciseTotal), total);
    }
}
