package com.buffetrestaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 20) @Pattern(regexp = PHONE_PATTERN, message = PHONE_MESSAGE) String phoneNumber
) {
    /** Optional; digits with an optional leading + and spaces, hyphens or parentheses. */
    public static final String PHONE_PATTERN = "^\\s*(\\+?[0-9][0-9 ()\\-]*)?\\s*$";
    public static final String PHONE_MESSAGE = "must contain only digits, spaces, hyphens, parentheses and an optional leading +";
}
