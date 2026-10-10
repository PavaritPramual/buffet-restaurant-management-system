package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;

public interface SessionContextProvider {
    /** Non-null ACTIVE context matching the ID; missing/invalid credentials must throw. */
    SessionContextSnapshot requireSession(Long sessionId, String sessionToken);

    /** Runtime implementations must lock/recheck in the caller transaction and reject bill-requested sessions. */
    SessionContextSnapshot requireSessionForOrder(Long sessionId, String customerCredential);

    record SessionContextSnapshot(
            Long sessionId,
            Long packageId,
            String tableNumber,
            DiningSessionStatus status
    ) {}
}
