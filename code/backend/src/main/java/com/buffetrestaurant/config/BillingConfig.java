package com.buffetrestaurant.config;

import com.buffetrestaurant.service.billing.BillCalculationStrategy;
import com.buffetrestaurant.service.billing.BillingEngine;
import com.buffetrestaurant.service.billing.StandardBillCalculation;
import com.buffetrestaurant.service.billing.ChildRateCalculationStrategy;
import com.buffetrestaurant.service.billing.DiscountCalculationStrategy;
import com.buffetrestaurant.service.billing.PromotionDiscountStrategy;
import java.math.BigDecimal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingConfig {

    @Bean
    public BillCalculationStrategy billCalculationStrategy() {
        return new StandardBillCalculation(new ChildRateCalculationStrategy(new BigDecimal("0.5")));
    }

    @Bean
    public DiscountCalculationStrategy discountCalculationStrategy() {
        return new PromotionDiscountStrategy();
    }

    @Bean
    public BillingEngine billingEngine(BillCalculationStrategy strategy, DiscountCalculationStrategy discount) {
        return new BillingEngine(strategy, discount);
    }
}
