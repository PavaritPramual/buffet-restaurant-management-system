package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/** Manager operations do not need the staff QR rendering credential. */
public record ManagerDiningSessionResponse(Long sessionId, Long tableId, String tableNumber,
        DiningSessionStatus sessionStatus, int adultCount, int childCount,
        OffsetDateTime startTime, OffsetDateTime endTime) {
    public static ManagerDiningSessionResponse from(DiningSession session) {
        return new ManagerDiningSessionResponse(session.getId(), session.getRestaurantTable().getId(),
                session.getRestaurantTable().getTableNumber(), session.getStatus(), session.getAdultCount(),
                session.getChildCount(), session.getStartTime().atOffset(ZoneOffset.UTC),
                session.getEndTime() == null ? null : session.getEndTime().atOffset(ZoneOffset.UTC));
    }
}
