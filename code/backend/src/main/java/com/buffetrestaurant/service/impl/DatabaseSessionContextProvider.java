package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.service.SessionContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ordering.session-provider", havingValue = "database", matchIfMissing = true)
public class DatabaseSessionContextProvider implements SessionContextProvider {
    private final CustomerSessionAccessService customerAccess;

    public DatabaseSessionContextProvider(CustomerSessionAccessService customerAccess) {
        this.customerAccess = customerAccess;
    }

    @Override
    public SessionContextSnapshot requireSession(Long sessionId, String sessionToken) {
        return customerAccess.requireSession(sessionId, sessionToken, false);
    }

    @Override
    public SessionContextSnapshot requireSessionForOrder(Long sessionId, String customerCredential) {
        return customerAccess.requireSession(sessionId, customerCredential, true);
    }
}
