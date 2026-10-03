package com.buffetrestaurant.config;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.service.AuthService;
import com.buffetrestaurant.service.StockService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("demo")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoDataSeeder {
    @Bean
    ApplicationRunner seedDemoData(UserAccountRepository users, StockItemRepository stockItems,
            AuthService authService, StockService stockService) {
        return args -> {
            if (users.findByUsername("admin").isEmpty()) {
                authService.createUser(new CreateUserRequest("admin", "admin123", "ผู้จัดการตัวอย่าง",
                        "admin@example.test", UserRole.MANAGER));
            }
            UserAccount user = users.findByUsername("admin").orElseThrow();

            List<StockItem> seededItems = List.of(
                    new StockItem("ING-001", "ข้าวหอมมะลิ", "กก.", BigDecimal.ZERO, new BigDecimal("8.000")),
                    new StockItem("ING-002", "นมสด", "ลิตร", BigDecimal.ZERO, new BigDecimal("5.000")),
                    new StockItem("ING-003", "สันคอหมู", "กก.", BigDecimal.ZERO, new BigDecimal("4.000"))
            );
            UserContext context = new UserContext(user.getId(), user.getUsername(), "ผู้จัดการตัวอย่าง", user.getRole());
            for (StockItem seed : seededItems) {
                if (stockItems.findBySku(seed.getSku()).isEmpty()) {
                    StockItem saved = stockItems.save(seed);
                    stockService.stockIn(saved.getId(), new StockInRequest(new BigDecimal("20.000"),
                            "สต็อกตั้งต้นสำหรับทดลอง"), context);
                }
            }
        };
    }
}