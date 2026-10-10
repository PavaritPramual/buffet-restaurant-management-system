package com.buffetrestaurant.config;

import com.buffetrestaurant.exception.ForbiddenException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerOriginGuard {
    private final String[] allowedOrigins;

    public CustomerOriginGuard(@Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins.clone();
    }

    public void requireAllowed(String origin) {
        if (origin == null || Arrays.stream(allowedOrigins).noneMatch(origin::equals)) {
            throw new ForbiddenException("Customer request origin is not allowed");
        }
    }
}
