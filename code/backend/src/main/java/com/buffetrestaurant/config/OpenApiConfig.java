package com.buffetrestaurant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI buffetRestaurantOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Buffet Restaurant Management System API")
                        .version("v1")
                        .description("Shared REST API contract for the buffet restaurant management system. "
                                + "Staff authentication uses the JSESSIONID cookie from POST /api/v1/auth/login. "
                                + "Customer access uses an HttpOnly customer_session cookie set by POST "
                                + "/api/v1/dining-sessions/qr-exchange. Do not send cookie credentials in request bodies "
                                + "or include real credentials in examples."))
                .components(new Components()
                        .addSecuritySchemes("staffSessionCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Issued by the staff login endpoint. Never share or add a real session ID to examples."))
                        .addSecuritySchemes("customerSessionCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("customer_session")
                                .description("HttpOnly cookie issued after QR exchange. Never share or add a real cookie or QR credential to examples.")));
    }
}
