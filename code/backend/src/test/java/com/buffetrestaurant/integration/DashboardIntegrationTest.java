package com.buffetrestaurant.integration;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.config.import=",
        "spring.datasource.url=jdbc:h2:mem:dashboard_test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "app.fulfillment.access-provider=session", "app.billing.context-provider=database", "app.payment.status-provider=database"})
class DashboardIntegrationTest extends DashboardScenarios {}
