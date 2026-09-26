package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.service.SessionContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ordering.session-provider", havingValue = "database", matchIfMissing = true)
public class DatabaseSessionContextProvider implements SessionContextProvider {
    private final DiningSessionRepository sessionRepository;

    public DatabaseSessionContextProvider(DiningSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public SessionContextSnapshot requireSession(Long sessionId, String sessionToken) {
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new ResourceNotFoundException("Active dining session not found");
        }

        return sessionRepository.findByIdAndSessionTokenAndStatus(
                        sessionId, sessionToken, DiningSessionStatus.ACTIVE)
                .map(session -> new SessionContextSnapshot(
                        session.getId(),
                        session.getBuffetPackage().getId(),
                        session.getRestaurantTable().getTableNumber(),
                        session.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
    }
}
