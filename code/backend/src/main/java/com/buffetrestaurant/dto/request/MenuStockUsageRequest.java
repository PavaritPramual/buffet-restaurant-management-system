package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MenuStockUsageRequest(@NotNull @Positive Long stockItemId,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 9, fraction = 3)
        BigDecimal quantityPerServing) {}
