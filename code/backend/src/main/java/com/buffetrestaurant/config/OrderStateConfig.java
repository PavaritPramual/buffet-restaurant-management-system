package com.buffetrestaurant.config;

import com.buffetrestaurant.service.state.OrderState;
import com.buffetrestaurant.service.state.ReceivedState;
import com.buffetrestaurant.service.state.PreparingState;
import com.buffetrestaurant.service.state.ReadyState;
import com.buffetrestaurant.service.state.ServedState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers the existing stateless singleton policies without changing transitions. */
@Configuration(proxyBeanMethods = false)
public class OrderStateConfig {
    @Bean OrderState receivedOrderState() { return ReceivedState.INSTANCE; }
    @Bean OrderState preparingOrderState() { return PreparingState.INSTANCE; }
    @Bean OrderState readyOrderState() { return ReadyState.INSTANCE; }
    @Bean OrderState servedOrderState() { return ServedState.INSTANCE; }
}
