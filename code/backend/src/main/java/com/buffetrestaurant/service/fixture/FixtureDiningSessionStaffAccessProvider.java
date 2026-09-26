package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.service.DiningSessionStaffAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
@ConditionalOnProperty(name = "app.dining-session.staff-access-provider", havingValue = "fixture")
public class FixtureDiningSessionStaffAccessProvider implements DiningSessionStaffAccessProvider {
    @Override
    public void requireServiceStaffAccess() {
        // Development/test fixture only. Authentication must replace this in deployed environments.
    }
}
