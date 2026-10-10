package com.buffetrestaurant.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties={"spring.config.import=", "spring.datasource.url=jdbc:h2:mem:menu_stock_v19;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "app.ordering.session-provider=database", "app.menu.admin-access-provider=session", "app.fulfillment.access-provider=session"})
@AutoConfigureMockMvc @ActiveProfiles("test")
class MenuStockConsumptionIntegrationTest extends MenuStockConsumptionScenarios {}
