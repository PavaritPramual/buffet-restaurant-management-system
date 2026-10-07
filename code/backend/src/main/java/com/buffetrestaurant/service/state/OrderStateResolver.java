package com.buffetrestaurant.service.state;

import com.buffetrestaurant.domain.enums.OrderStatus;

/** Resolves a non-null status to its registered state; never returns null. */
public interface OrderStateResolver {
    OrderState resolve(OrderStatus status);
}
