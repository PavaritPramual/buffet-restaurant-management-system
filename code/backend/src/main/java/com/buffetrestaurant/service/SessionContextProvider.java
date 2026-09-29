package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;

public interface SessionContextProvider {
    SessionContextSnapshot requireSession(Long sessionId, String sessionToken);

    default SessionContextSnapshot requireSessionForOrder(Long sessionId, String customerCredential) {
        return requireSession(sessionId, customerCredential);
    }

    record SessionContextSnapshot(
            Long sessionId,
            Long packageId,
            String tableNumber,
            DiningSessionStatus status
    ) {}
}
