package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class SessionPaymentAccessProvider implements PaymentAccessProvider {

    private final UserContextProvider users;

    public SessionPaymentAccessProvider(UserContextProvider users) {
        this.users = users;
    }

    @Override
    public void requirePaymentAccess() {
        users.requireCurrentRequestRole(UserRole.SERVICE_STAFF);
    }
}