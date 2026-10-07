package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotNull;

public record StockItemActiveRequest(@NotNull Boolean active) {}
