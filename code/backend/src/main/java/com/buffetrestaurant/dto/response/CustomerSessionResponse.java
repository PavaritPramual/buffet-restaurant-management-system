package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;

public record CustomerSessionResponse(
        Long sessionId,
        Long packageId,
        String tableNumber,
        DiningSessionStatus sessionStatus
) {
}
