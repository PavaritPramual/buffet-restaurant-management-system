package com.buffetrestaurant.service.billing;

import java.math.BigDecimal;

import com.buffetrestaurant.dto.billing.BillingContext;

public interface BillCalculationStrategy {
    
    BigDecimal calculate(BillingContext context);
}
