package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForceOperationRequest(
        @NotBlank(message = "กรุณาระบุเหตุผล") @Size(max = 500, message = "เหตุผลต้องไม่เกิน 500 ตัวอักษร") String reason
) {}
