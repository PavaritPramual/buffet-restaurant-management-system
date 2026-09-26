package com.buffetrestaurant.dto.response;

import java.math.BigDecimal;

public record BuffetPackageResponse(
        Long id,
        String name,
        BigDecimal price,
        String description,
        boolean active
) {
}