package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.UserRole;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.master-data.access-provider", havingValue = "session", matchIfMissing = true)
public class SessionMasterDataRemovalAccessProvider implements MasterDataRemovalAccessProvider {
    private final UserContextProvider users;

    public SessionMasterDataRemovalAccessProvider(UserContextProvider users) { this.users = users; }

    @Override public void requireManagerAccess() { users.requireCurrentRequestRole(UserRole.MANAGER); }
}
