package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Authorizes fulfillment from the authenticated server session, never a caller role header. */
@Component
@ConditionalOnProperty(name = "app.fulfillment.access-provider", havingValue = "session")
public class SessionOrderFulfillmentAccessProvider implements OrderFulfillmentAccessProvider {
    private final SessionUserContextProvider users;

    public SessionOrderFulfillmentAccessProvider(SessionUserContextProvider users) {
        this.users = users;
    }

    @Override
    public void requireKitchenAccess() {
        users.requireCurrentRequestRole(UserRole.KITCHEN_STAFF);
    }

    @Override
    public void requireServiceStaffAccess() {
        users.requireCurrentRequestRole(UserRole.SERVICE_STAFF);
    }
}
