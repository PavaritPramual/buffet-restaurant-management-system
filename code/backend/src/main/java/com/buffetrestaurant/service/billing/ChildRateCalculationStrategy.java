package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import java.math.BigDecimal;

/** Immutable child-rate strategy, independent of per-request bill data. */
public class ChildRateCalculationStrategy implements BillCalculationStrategy {
    private final BigDecimal rate;
    public ChildRateCalculationStrategy(BigDecimal rate) {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0)
            throw new IllegalArgumentException("Child rate must be between 0 and 1");
        this.rate = rate;
    }
    @Override
    public BigDecimal calculate(BillingContext context) {
        if (context == null || context.getPackagePrice() == null || context.getChildCount() == null
                || context.getPackagePrice().signum() < 0 || context.getChildCount() < 0)
            throw new IllegalArgumentException("Valid price and child count are required");
        return context.getPackagePrice().multiply(rate).multiply(BigDecimal.valueOf(context.getChildCount()));
    }
}
