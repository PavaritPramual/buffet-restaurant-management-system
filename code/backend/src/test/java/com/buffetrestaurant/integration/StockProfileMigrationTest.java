package com.buffetrestaurant.integration;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockProfileMigrationTest {
    @Test
    void h2V15AddsTargetActiveAndProfileColumnsWithoutChangingLegacyRows() {
        String url = "jdbc:h2:mem:stock_profile_upgrade_" + UUID.randomUUID()
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        StockProfileMigrationAssertions.assertV15PreservesLegacyRows(url, "sa", "",
                "classpath:db/migration/common", "classpath:db/migration/h2");
    }
}
