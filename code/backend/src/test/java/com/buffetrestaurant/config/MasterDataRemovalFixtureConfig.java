package com.buffetrestaurant.config;

import com.buffetrestaurant.service.MasterDataRemovalAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Legacy isolated tests only. No bypass implementation exists in the runtime artifact. */
@Configuration
@Profile({"test", "demo", "postgres-it"})
@ConditionalOnProperty(name = "app.master-data.access-provider", havingValue = "disabled")
public class MasterDataRemovalFixtureConfig {
    @Bean @org.springframework.context.annotation.Primary
    MasterDataRemovalAccessProvider fixtureRemovalAccess() { return () -> {}; }
}
