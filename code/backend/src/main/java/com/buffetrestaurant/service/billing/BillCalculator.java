package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;

/** Calculates a non-null bill from trusted context without recording payment. */
public interface BillCalculator {
    BillCalculation calculate(BillingContext context);
}
