package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OpenDiningSessionRequest(
        @NotNull @Positive Long tableId,
        @NotNull @Positive Long packageId,
        @NotNull @Positive Long soupId,
        @NotNull @Min(0) Integer adultCount,
        @Min(0) Integer childCount
) {
    public OpenDiningSessionRequest {
        if (childCount == null) {
            childCount = 0;
        }
    }
}
