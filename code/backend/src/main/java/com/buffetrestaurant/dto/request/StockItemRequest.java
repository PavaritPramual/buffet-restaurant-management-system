package com.buffetrestaurant.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record StockItemRequest(@NotBlank @Size(max=40) String sku,
        @NotBlank @Size(max=120) String name, @NotBlank @Size(max=24) String unit,
        @NotNull @DecimalMin("0") @Digits(integer=9, fraction=3) BigDecimal lowStockThreshold,
        @DecimalMin("0") @Digits(integer=9, fraction=3) BigDecimal openingTargetStock) {}
