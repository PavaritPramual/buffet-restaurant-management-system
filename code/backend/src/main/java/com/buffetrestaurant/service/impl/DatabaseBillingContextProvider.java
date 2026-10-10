package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.integration.billing.DiningSessionBillingReader;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "app.billing.context-provider",
        havingValue = "database"
)
public class DatabaseBillingContextProvider implements BillingContextProvider {

    private final DiningSessionBillingReader sessionReader;

    public DatabaseBillingContextProvider(
            DiningSessionBillingReader sessionReader
    ) {
        this.sessionReader = sessionReader;
    }

    @Override
    public BillingContext findBySessionId(Long sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("A positive session ID is required");
        }

        var snapshot = sessionReader.requireBySessionId(sessionId);
        if (snapshot == null || !sessionId.equals(snapshot.sessionId())
                || snapshot.packagePriceAtOpen() == null || snapshot.packagePriceAtOpen().signum() < 0
                || snapshot.adultCount() == null || snapshot.childCount() == null
                || snapshot.adultCount() < 0 || snapshot.childCount() < 0
                || (long) snapshot.adultCount() + snapshot.childCount() < 1
                || snapshot.sessionStatus() == null) {
            throw new IllegalStateException("Billing snapshot does not match the requested session contract");
        }

        BillingContext context = new BillingContext();
        context.setSessionId(snapshot.sessionId());
        context.setPackagePrice(snapshot.packagePriceAtOpen());
        context.setAdultCount(snapshot.adultCount());
        context.setChildCount(snapshot.childCount());
        context.setSessionStatus(snapshot.sessionStatus());
        context.setDiscountContext(null);

        return context;
    }
}
