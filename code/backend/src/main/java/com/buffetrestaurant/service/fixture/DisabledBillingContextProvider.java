package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.billing.context-provider",
        havingValue = "disabled",
        matchIfMissing = true
)
public class DisabledBillingContextProvider implements BillingContextProvider {

    @Override
    public BillingContext findBySessionId(Long sessionId) {
        throw new ServiceUnavailableException(
                "Billing and payment are unavailable until billing session data is configured"
        );
    }
}