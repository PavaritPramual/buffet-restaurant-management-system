package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.service.SessionContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "test"})
@ConditionalOnProperty(name = "app.ordering.session-provider", havingValue = "fixture")
public class FixtureSessionContextProvider implements SessionContextProvider {
    @Override
    public SessionContextSnapshot requireSession(Long sessionId, String sessionToken) {
        if (Long.valueOf(1L).equals(sessionId) && "fixture-active-token".equals(sessionToken)) {
            return new SessionContextSnapshot(1L, 1L, "T01", DiningSessionStatus.ACTIVE);
        }
        throw new ResourceNotFoundException("Active dining session not found");
    }
}
