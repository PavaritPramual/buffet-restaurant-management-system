package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import java.time.OffsetDateTime;

public record DiningSessionResponse(
        Long sessionId,
        String sessionToken,
        Long packageId,
        Long tableId,
        String tableNumber,
        Long soupId,
        DiningSessionStatus sessionStatus,
        Integer adultCount,
        Integer childCount,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
