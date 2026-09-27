package com.buffetrestaurant.config;

import com.buffetrestaurant.service.billing.BillCalculationStrategy;
import com.buffetrestaurant.service.billing.BillingEngine;
import com.buffetrestaurant.service.billing.StandardBillCalculation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingConfig {

    @Bean
    public BillCalculationStrategy billCalculationStrategy() {
        return new StandardBillCalculation();
    }

    @Bean
    public BillingEngine billingEngine(BillCalculationStrategy strategy) {
        return new BillingEngine(strategy);
    }
}