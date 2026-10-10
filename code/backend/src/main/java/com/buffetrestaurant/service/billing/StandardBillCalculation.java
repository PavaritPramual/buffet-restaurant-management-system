package com.buffetrestaurant.service.billing;

import java.math.BigDecimal;

import com.buffetrestaurant.dto.billing.BillingContext;

public class StandardBillCalculation implements BillCalculationStrategy{
    private final BillCalculationStrategy childStrategy;

    public StandardBillCalculation() { this(new ChildRateCalculationStrategy(new BigDecimal("0.5"))); }

    public StandardBillCalculation(BillCalculationStrategy childStrategy) {
        this.childStrategy = java.util.Objects.requireNonNull(childStrategy);
    }

    

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
        BigDecimal childTotal = childStrategy.calculate(context);
        
        return adultTotal.add(childTotal);
    }
}