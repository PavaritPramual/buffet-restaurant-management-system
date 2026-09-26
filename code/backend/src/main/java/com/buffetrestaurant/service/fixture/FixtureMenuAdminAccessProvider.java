package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.service.MenuAdminAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
@ConditionalOnProperty(name = "app.menu.admin-access-provider", havingValue = "fixture")
public class FixtureMenuAdminAccessProvider implements MenuAdminAccessProvider {
    @Override
    public void requireMenuWriteAccess() {
        // Development/test fixture only. Never enable this provider in a deployed environment.
    }
}
