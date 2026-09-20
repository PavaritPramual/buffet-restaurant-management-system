package com.buffetrestaurant.mapper;

import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.response.TableResponse;
import org.springframework.stereotype.Component;

@Component
public class TableMapper {

    public TableResponse toResponse(RestaurantTable table) {
        if (table == null) {
            return null;
        }
        return new TableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus()
        );
    }

    public RestaurantTable toEntity(CreateTableRequest request) {
        if (request == null) {
            return null;
        }
        return new RestaurantTable(
                request.tableNumber(),
                request.capacity()
        );
    }
}
