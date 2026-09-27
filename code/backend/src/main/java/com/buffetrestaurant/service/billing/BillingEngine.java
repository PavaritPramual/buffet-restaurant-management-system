package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.dto.response.BillSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BillingEngine {
    
    private final BillCalculationStrategy strategys;
    
    public BillingEngine(BillCalculationStrategy strategy){
        if (strategy == null){
            throw new IllegalArgumentException("Calculation Strategy required");
        }
        this.strategys = strategy;
    }

    public BillSummary calculate(BillingContext context){
        if (context == null){
            throw new IllegalArgumentException("Billing context is required");
        }
        
        if (context.getSessionId() == null){
            throw new IllegalArgumentException("Session ID is required");
        }

        BigDecimal supTotalNoneDiscount = strategys.calculate(context);
        BillSummary summary = new BillSummary();

        if (context.getDiscountContext() != null){
            
            Object percentage = context.getDiscountContext().get("percentage");

            if (!(percentage instanceof Number)) {
                throw new IllegalArgumentException("Percentage must be a number");
            }

            BigDecimal discountPercentage = new BigDecimal(percentage.toString());

            if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 || discountPercentage.compareTo(new BigDecimal("100")) > 0){
                throw new IllegalArgumentException("Percentage must be between 0 to 100");
            }

            BigDecimal discountRate = discountPercentage.divide(new BigDecimal("100"));

            BigDecimal discount = supTotalNoneDiscount.multiply(discountRate);

            BigDecimal total = supTotalNoneDiscount.subtract(discount).setScale(2, RoundingMode.HALF_UP);

            summary.setDiscountAmount(discount);
            summary.setTotalAmount(total);
        }else{
            summary.setDiscountAmount(BigDecimal.ZERO);
            summary.setTotalAmount(supTotalNoneDiscount.setScale(2, RoundingMode.HALF_UP));
        }
        
        BigDecimal totalBeforeRounding = supTotalNoneDiscount.subtract(summary.getDiscountAmount());

        BigDecimal roundingAdjustment = summary.getTotalAmount().subtract(totalBeforeRounding);

        summary.setTotalBeforeRounding(totalBeforeRounding);
        summary.setRoundingAdjustment(roundingAdjustment);

        summary.setSessionId(context.getSessionId());
        summary.setSubtotalNoneDiscount(supTotalNoneDiscount);
        
        return summary;
    }


}
