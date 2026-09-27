package com.buffetrestaurant.dto.response;

import java.util.Set;

public record MenuItemResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        boolean available,
        Set<Long> packageIds,
        String imageUrl
) {}
