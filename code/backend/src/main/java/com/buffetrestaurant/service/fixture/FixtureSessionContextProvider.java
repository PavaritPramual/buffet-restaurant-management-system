package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.service.SessionContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ordering.session-provider", havingValue = "fixture", matchIfMissing = true)
public class FixtureSessionContextProvider implements SessionContextProvider {
    @Override
    public SessionContextSnapshot requireSession(Long sessionId) {
        if (Long.valueOf(1L).equals(sessionId)) {
            return new SessionContextSnapshot(1L, 1L, "T01", DiningSessionStatus.ACTIVE);
        }
        if (Long.valueOf(2L).equals(sessionId)) {
            return new SessionContextSnapshot(2L, 1L, "T02", DiningSessionStatus.COMPLETED);
        }
        throw new ResourceNotFoundException("Dining session not found with id: " + sessionId);
    }
}
