package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotNull;

public record UserActiveRequest(@NotNull Boolean active) {}
