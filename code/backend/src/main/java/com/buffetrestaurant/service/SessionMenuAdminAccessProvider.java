package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.menu.admin-access-provider", havingValue = "session")
public class SessionMenuAdminAccessProvider implements MenuAdminAccessProvider {
    private final SessionUserContextProvider users;

    public SessionMenuAdminAccessProvider(SessionUserContextProvider users) {
        this.users = users;
    }

    @Override
    public void requireMenuWriteAccess() {
        users.requireCurrentRequestRole(UserRole.MANAGER);
    }
}