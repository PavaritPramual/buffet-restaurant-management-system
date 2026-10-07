package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ExchangeQrRequest(
        @NotBlank @Size(max = 100)
        @Schema(description = "Single-use QR credential; submit only to POST /qr-exchange. Never use a real value in examples.",
                accessMode = Schema.AccessMode.WRITE_ONLY)
        String token
) {
}
