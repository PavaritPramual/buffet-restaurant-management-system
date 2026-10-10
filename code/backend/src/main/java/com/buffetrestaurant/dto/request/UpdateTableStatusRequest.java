package com.buffetrestaurant.dto.request;

import com.buffetrestaurant.domain.enums.TableStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTableStatusRequest(
        @NotNull(message = "Status is required")
        TableStatus status
) {
}
