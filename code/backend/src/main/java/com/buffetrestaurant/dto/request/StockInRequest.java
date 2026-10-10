package com.buffetrestaurant.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StockInRequest(
        @NotNull @DecimalMin(value = "0.001") @Digits(integer = 9, fraction = 3)
        @Schema(description = "Quantity received; JSON number greater than zero, up to three decimal places",
                type = "number", format = "double", implementation = Double.class, example = "12.500")
        BigDecimal quantity,
        @NotBlank @Pattern(regexp = ".*\\S.*", message = "must contain a non-whitespace character")
        @Size(max = 255)
        @Schema(description = "Non-blank audit reason", example = "Delivery received")
        String reason
) {
}