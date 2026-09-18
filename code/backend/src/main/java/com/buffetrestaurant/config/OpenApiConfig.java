package com.buffetrestaurant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI buffetRestaurantOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Buffet Restaurant Management System API")
                .version("v1")
                .description("Shared REST API contract for the buffet restaurant management system."));
    }
}
