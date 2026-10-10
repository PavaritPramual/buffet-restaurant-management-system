package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

public record StockAdjustmentRequest(
                @NotNull
                @DecimalMin(value = "-999999999.999")
                @DecimalMax(value = "999999999.999")
                @Digits(integer = 9, fraction = 3)
                @Schema(description = "Signed quantity change; non-zero JSON number with up to three decimal places",
                        type = "number", format = "double", implementation = Double.class, example = "-1.250")
                BigDecimal quantityDelta,
                @NotBlank @Pattern(regexp = ".*\\S.*", message = "must contain a non-whitespace character")
                @Size(max = 255)
                @Schema(description = "Non-blank audit reason", example = "Count correction")
                String reason
) {
        @AssertTrue(message = "quantityDelta must be non-zero")
        public boolean isNonZeroQuantityDelta() {
                return quantityDelta == null || quantityDelta.signum() != 0;
        }
}