package com.buffetrestaurant.config;

import com.buffetrestaurant.exception.ForbiddenException;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerOriginGuard {
    @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}")
    private String[] allowedOrigins;

    public void requireAllowed(String origin) {
        if (origin == null || Arrays.stream(allowedOrigins).noneMatch(origin::equals)) {
            throw new ForbiddenException("Customer request origin is not allowed");
        }
    }
}
