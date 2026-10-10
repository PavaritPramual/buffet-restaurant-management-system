package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.TableStatus;

public record TableResponse(
        Long id,
        String tableNumber,
        Integer capacity,
        TableStatus status,
        boolean archived
) {
    public TableResponse(Long id, String tableNumber, Integer capacity, TableStatus status) {
        this(id, tableNumber, capacity, status, false);
    }
}
