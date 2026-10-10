package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.dining-session.staff-access-provider", havingValue = "session")
public class SessionDiningSessionStaffAccessProvider implements DiningSessionStaffAccessProvider {
    private final UserContextProvider users;

    public SessionDiningSessionStaffAccessProvider(UserContextProvider users) {
        this.users = users;
    }

    @Override
    public void requireServiceStaffAccess() {
        users.requireCurrentRequestRole(UserRole.SERVICE_STAFF);
    }
}
