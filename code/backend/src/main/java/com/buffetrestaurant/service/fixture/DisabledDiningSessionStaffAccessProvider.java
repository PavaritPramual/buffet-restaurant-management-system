package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.DiningSessionStaffAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.dining-session.staff-access-provider", havingValue = "disabled", matchIfMissing = true)
public class DisabledDiningSessionStaffAccessProvider implements DiningSessionStaffAccessProvider {
    @Override
    public void requireServiceStaffAccess() {
        throw new ServiceUnavailableException(
                "Staff table operations are unavailable until staff authorization is configured");
    }
}
