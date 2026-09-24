package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record MenuItemRequest(
        @NotNull @Positive Long categoryId,
        @NotBlank @Size(max = 120) String name,
        boolean available,
        @NotEmpty Set<@Positive Long> packageIds,
        @Size(max = 500)
        @Pattern(regexp = "^(https?://.+|/.*)?$", message = "must be an http(s) URL or an absolute site path")
        String imageUrl
) {}
