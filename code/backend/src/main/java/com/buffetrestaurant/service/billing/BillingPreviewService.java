package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.dto.response.BillSummary;
import com.buffetrestaurant.exception.BusinessRuleException;
import org.springframework.stereotype.Service;

/** Resolves trusted session data before calculating a bill; never records a payment. */
@Service
public class BillingPreviewService {
    private final BillingContextProvider contextProvider;
    private final BillingEngine billingEngine;

    public BillingPreviewService(BillingContextProvider contextProvider, BillingEngine billingEngine) {
        this.contextProvider = contextProvider;
        this.billingEngine = billingEngine;
    }

    public BillSummary preview(Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("A positive session ID is required");
        }
        BillingContext context = contextProvider.findBySessionId(sessionId);
        if (context == null || !sessionId.equals(context.getSessionId())) {
            throw new IllegalStateException("Billing context does not match the requested session");
        }
        if (context.getSessionStatus() != DiningSessionStatus.ACTIVE) {
            throw new BusinessRuleException("Only ACTIVE sessions can be calculated for payment");
        }
        return billingEngine.calculate(context);
    }
}
