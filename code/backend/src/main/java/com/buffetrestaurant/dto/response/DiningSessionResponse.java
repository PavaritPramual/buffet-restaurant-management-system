package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record DiningSessionResponse(
        Long sessionId,
        @Schema(description = "Staff-only QR rendering credential; never include a live value in Swagger examples.",
                accessMode = Schema.AccessMode.READ_ONLY)
        String sessionToken,
        Long packageId,
        Long tableId,
        String tableNumber,
        Long soupId,
        DiningSessionStatus sessionStatus,
        Integer adultCount,
        Integer childCount,
        @Schema(description = "ISO-8601 UTC timestamp", example = "2026-10-07T07:00:00Z")
        OffsetDateTime startTime,
        @Schema(description = "ISO-8601 UTC timestamp; null while active", example = "2026-10-07T08:00:00Z",
                nullable = true)
        OffsetDateTime endTime,
        @Schema(description = "ISO-8601 UTC timestamp; null until requested", example = "2026-10-07T07:45:00Z",
                nullable = true)
        OffsetDateTime billRequestedAt,
        @Schema(description = "Current package display name, not a name snapshot; billing uses the price at open.",
                example = "บุฟเฟต์มาตรฐาน", accessMode = Schema.AccessMode.READ_ONLY)
        String packageName,
        @Schema(description = "Current soup display name, including inactive catalog entries.",
                example = "น้ำซุปใส", accessMode = Schema.AccessMode.READ_ONLY)
        String soupName
) {
}
