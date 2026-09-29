package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;

/** Resolves an {@link OrderStatus} to its {@link OrderState} singleton. */
public final class OrderStateFactory {

    private OrderStateFactory() {
    }

    public static OrderState forStatus(OrderStatus status) {
        return switch (status) {
            case RECEIVED -> ReceivedState.INSTANCE;
            case PREPARING -> PreparingState.INSTANCE;
            case READY -> ReadyState.INSTANCE;
            case SERVED -> ServedState.INSTANCE;
        };
    }
}
