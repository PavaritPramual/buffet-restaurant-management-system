package com.buffetrestaurant.service.billing;

import java.math.BigDecimal;

import com.buffetrestaurant.dto.billing.BillingContext;

public class StandardBillCalculation implements BillCalculationStrategy{

    

    @Override 
    public BigDecimal calculate(BillingContext context){

        if (context == null) {
            throw new IllegalArgumentException("Billing context is required");
        }

        if (context.getPackagePrice() == null
                || context.getAdultCount() == null
                || context.getChildCount() == null) {
            throw new IllegalArgumentException("Price and customer counts are required");
        }

        if (context.getPackagePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Package price must not be negative");
        }

        if (context.getAdultCount() < 0 || context.getChildCount() < 0) {
            throw new IllegalArgumentException("Customer counts must not be negative");
        }

        if (context.getAdultCount() == 0 && context.getChildCount() == 0) {
            throw new IllegalArgumentException("At least one customer is required");
        }

        BigDecimal packagePrice = context.getPackagePrice();

        BigDecimal adultTotal = packagePrice.multiply(BigDecimal.valueOf(context.getAdultCount()));
        BigDecimal childTotal = packagePrice.multiply(new BigDecimal("0.5")).multiply(BigDecimal.valueOf(context.getChildCount()));
        
        return adultTotal.add(childTotal);
    }
}