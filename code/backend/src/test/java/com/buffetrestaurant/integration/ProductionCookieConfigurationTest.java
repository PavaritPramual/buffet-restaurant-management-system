package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;
import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.buffetrestaurant.repository.SoupRepository;
import com.buffetrestaurant.service.AuthService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:production_cookie_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "CORS_ALLOWED_ORIGINS=https://billing.example.test",
        "CUSTOMER_COOKIE_SECURE=false"
})
@ActiveProfiles({"test", "production"})
class ProductionCookieConfigurationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private Environment environment;

    @Autowired
    private AuthService authService;

    @Autowired
    private RestaurantTableRepository tables;

    @Autowired
    private BuffetPackageRepository packages;

    @Autowired
    private SoupRepository soups;

    @Autowired
    private DiningSessionRepository sessions;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void staffAndCustomerCookiesAreSecureInProduction() throws Exception {
        assertThat(environment.getProperty("app.customer-cookie.secure")).isEqualTo("true");
        authService.createUser(new CreateUserRequest("test-staff", "password123", "Test Staff",
                null, UserRole.SERVICE_STAFF, "Test", "Staff", null));
        HttpResponse<String> staffResponse = post("/api/v1/auth/login",
                "{\"username\":\"test-staff\",\"password\":\"password123\"}", false);
        assertThat(staffResponse.statusCode()).isEqualTo(200);
        String staffCookie = staffResponse.headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(staffCookie).contains("JSESSIONID=", "HttpOnly", "Secure", "SameSite=Lax");

        RestaurantTable table = tables.save(new RestaurantTable("T-Cookie", 4));
        BuffetPackage buffetPackage = packages.save(new BuffetPackage("Cookie package", new BigDecimal("299.00"), null));
        Soup soup = soups.save(new Soup("Cookie soup"));
        sessions.save(new DiningSession(table, buffetPackage, soup, 1, 0,
                "qr-token", LocalDateTime.now()));
        HttpResponse<String> customerResponse = post("/api/v1/dining-sessions/qr-exchange",
                "{\"token\":\"qr-token\"}", true);
        assertThat(customerResponse.statusCode()).isEqualTo(200);
        String customerCookie = customerResponse.headers().firstValue("Set-Cookie").orElseThrow();
        assertThat(customerCookie).contains("customer_session=", "HttpOnly", "Secure", "SameSite=Lax");
    }

    private HttpResponse<String> post(String path, String body, boolean withOrigin) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (withOrigin) request.header("Origin", "https://billing.example.test");
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
