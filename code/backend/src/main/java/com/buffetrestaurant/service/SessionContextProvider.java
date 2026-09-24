package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;

public interface SessionContextProvider {
    SessionContextSnapshot requireSession(Long sessionId);

    record SessionContextSnapshot(
            Long sessionId,
            Long packageId,
            String tableNumber,
            DiningSessionStatus status
    ) {}
}
