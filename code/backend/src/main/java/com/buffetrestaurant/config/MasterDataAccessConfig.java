package com.buffetrestaurant.config;

import com.buffetrestaurant.controller.BuffetPackageController;
import com.buffetrestaurant.controller.RestaurantTableController;
import com.buffetrestaurant.controller.SoupController;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.service.SessionUserContextProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Protects existing master-data endpoints without changing their DTOs or route contracts. */
@Configuration
@ConditionalOnProperty(name = "app.master-data.access-provider", havingValue = "session", matchIfMissing = true)
public class MasterDataAccessConfig implements WebMvcConfigurer {
    private final SessionUserContextProvider users;

    public MasterDataAccessConfig(SessionUserContextProvider users) { this.users = users; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if (!(handler instanceof HandlerMethod method)) return true;
                Class<?> controller = method.getBeanType();
                if (controller != RestaurantTableController.class && controller != BuffetPackageController.class
                        && controller != SoupController.class) return true;
                if ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod())) {
                    users.requireAnyRole(request, UserRole.SERVICE_STAFF, UserRole.MANAGER, UserRole.SUPERVISOR);
                } else {
                    users.requireAnyRole(request, UserRole.MANAGER);
                }
                return true;
            }
        });
    }
}
