package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Composition-time registration, validated once; resolution has no state-specific branches. */
@Component
public final class RegistryOrderStateResolver implements OrderStateResolver {
    private final Map<OrderStatus, OrderState> states;

    public RegistryOrderStateResolver(List<OrderState> registeredStates) {
        if (registeredStates == null) throw new IllegalArgumentException("Order states are required");
        var registry = new EnumMap<OrderStatus, OrderState>(OrderStatus.class);
        for (OrderState state : registeredStates) {
            if (state == null || state.status() == null) {
                throw new IllegalArgumentException("A registered order state must have a status");
            }
            if (registry.putIfAbsent(state.status(), state) != null) {
                throw new IllegalArgumentException("Duplicate order state: " + state.status());
            }
        }
        var missing = EnumSet.allOf(OrderStatus.class);
        missing.removeAll(registry.keySet());
        if (!missing.isEmpty()) throw new IllegalArgumentException("Missing order states: " + missing);
        this.states = Collections.unmodifiableMap(registry);
    }

    @Override
    public OrderState resolve(OrderStatus status) {
        if (status == null) throw new IllegalArgumentException("An order status is required");
        return states.get(status);
    }
}
