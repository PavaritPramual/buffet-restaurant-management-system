package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.MenuAdminAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.menu.admin-access-provider", havingValue = "disabled", matchIfMissing = true)
public class DisabledMenuAdminAccessProvider implements MenuAdminAccessProvider {
    @Override
    public void requireMenuWriteAccess() {
        throw new ServiceUnavailableException(
                "Menu administration is unavailable until staff authorization is configured"
        );
    }
}
