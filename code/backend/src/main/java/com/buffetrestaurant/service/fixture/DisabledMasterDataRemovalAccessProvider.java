package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.MasterDataRemovalAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.master-data.access-provider", havingValue = "disabled")
public class DisabledMasterDataRemovalAccessProvider implements MasterDataRemovalAccessProvider {
    @Override public void requireManagerAccess() {
        throw new ServiceUnavailableException("Master-data removal requires staff authorization configuration");
    }
}
