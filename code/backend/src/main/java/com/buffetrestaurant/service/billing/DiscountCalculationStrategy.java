package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import java.math.BigDecimal;

public interface DiscountCalculationStrategy {
    BigDecimal calculateDiscount(BillingContext context, BigDecimal subtotal);
}
