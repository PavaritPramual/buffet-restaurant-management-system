package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.dto.response.BillSummary;
import com.buffetrestaurant.service.billing.BillingEngine;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/billing")
public class BillingController {

    private final BillingEngine billingEngine;

    public BillingController(BillingEngine billingEngine) {
        this.billingEngine = billingEngine;
    }

    @PostMapping("/preview")
    public BillSummary preview(@RequestBody BillingContext context) {
        if (context.getSessionStatus() == null) {
            throw new IllegalArgumentException("Session status is required");
        }

        return billingEngine.calculate(context);
    }
}