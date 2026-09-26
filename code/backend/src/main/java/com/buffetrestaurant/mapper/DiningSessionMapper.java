package com.buffetrestaurant.mapper;

import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.dto.response.DiningSessionResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class DiningSessionMapper {
    public DiningSessionResponse toResponse(DiningSession diningSession) {
        return new DiningSessionResponse(
                diningSession.getId(),
                diningSession.getSessionToken(),
                diningSession.getBuffetPackage().getId(),
                diningSession.getRestaurantTable().getId(),
                diningSession.getRestaurantTable().getTableNumber(),
                diningSession.getSoup().getId(),
                diningSession.getStatus(),
                diningSession.getAdultCount(),
                diningSession.getChildCount(),
                asUtcOffset(diningSession.getStartTime()),
                diningSession.getEndTime() == null ? null : asUtcOffset(diningSession.getEndTime())
        );
    }

    private OffsetDateTime asUtcOffset(java.time.LocalDateTime value) {
        return value.atOffset(ZoneOffset.UTC);
    }
}
