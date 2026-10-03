package com.buffetrestaurant.config;

import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!demo & !test")
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class BootstrapAdminConfig {

    @Bean
    ApplicationRunner bootstrapInitialManager(
            UserAccountRepository users,
            AuthService authService,
            @Value("${app.bootstrap-admin.username:}") String username,
            @Value("${app.bootstrap-admin.password:}") String password,
            @Value("${app.bootstrap-admin.display-name:Restaurant Manager}") String displayName,
            @Value("${app.bootstrap-admin.email:}") String email
    ) {
        return args -> {
            if (users.count() > 0) return;
            authService.createUser(new CreateUserRequest(username, password, displayName, email, UserRole.MANAGER));
        };
    }
}