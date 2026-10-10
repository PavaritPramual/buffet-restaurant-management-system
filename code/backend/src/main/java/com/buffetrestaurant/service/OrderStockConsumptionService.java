package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.CustomerOrder;

/** Consumes the frozen recipe in the caller's locked-order transaction, or throws 409. */
public interface OrderStockConsumptionService {
    void consume(CustomerOrder order);
}
