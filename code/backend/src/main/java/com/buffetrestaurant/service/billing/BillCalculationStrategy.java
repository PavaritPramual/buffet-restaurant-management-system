package com.buffetrestaurant.service.billing;

import java.math.BigDecimal;

import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.dto.response.BillSummary;

public interface BillCalculationStrategy {
    
    BigDecimal calculate(BillingContext context);
}
