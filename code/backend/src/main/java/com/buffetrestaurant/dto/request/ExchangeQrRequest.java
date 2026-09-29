package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExchangeQrRequest(@NotBlank String token) {
}
