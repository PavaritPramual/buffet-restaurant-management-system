package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.service.SessionContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test", "demo"})
@ConditionalOnProperty(name = "app.ordering.session-provider", havingValue = "fixture")
public class FixtureSessionContextProvider implements SessionContextProvider {
    /** Controlled local/test/demo data only; this fixture does not prove database locking. */
    @Override
    public SessionContextSnapshot requireSessionForOrder(Long sessionId, String customerCredential) {
        return requireSession(sessionId, customerCredential);
    }
    @Override
    public SessionContextSnapshot requireSession(Long sessionId, String sessionToken) {
        if (Long.valueOf(1L).equals(sessionId) && "fixture-active-token".equals(sessionToken)) {
            return new SessionContextSnapshot(1L, 1L, "T01", DiningSessionStatus.ACTIVE);
        }
        throw new ResourceNotFoundException("Active dining session not found");
    }
}
