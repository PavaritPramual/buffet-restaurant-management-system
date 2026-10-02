package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.OrderFulfillmentAccessProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.fulfillment.access-provider", havingValue = "disabled", matchIfMissing = true)
public class DisabledOrderFulfillmentAccessProvider implements OrderFulfillmentAccessProvider {

    private static final String MESSAGE =
            "Order fulfillment authorization is unavailable until staff authentication is configured";

    @Override
    public void requireKitchenAccess() {
        throw new ServiceUnavailableException(MESSAGE);
    }

    @Override
    public void requireServiceStaffAccess() {
        throw new ServiceUnavailableException(MESSAGE);
    }
}
