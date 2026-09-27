package com.buffetrestaurant.service.fixture;

import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.service.SessionContextProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ordering.session-provider", havingValue = "disabled", matchIfMissing = true)
public class DisabledSessionContextProvider implements SessionContextProvider {
    @Override
    public SessionContextSnapshot requireSession(Long sessionId) {
        throw new ServiceUnavailableException(
                "Customer ordering is unavailable until Dining Session verification is configured"
        );
    }
}
