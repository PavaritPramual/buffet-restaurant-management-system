package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableStatusRequest;
import com.buffetrestaurant.dto.response.TableResponse;
import java.util.List;

public interface RestaurantTableService {

    List<TableResponse> getAllTables(TableStatus status);

    TableResponse getTableById(Long id);

    TableResponse createTable(CreateTableRequest request);

    TableResponse updateTable(Long id, UpdateTableRequest request);

    TableResponse updateTableStatus(Long id, UpdateTableStatusRequest request);

    void deleteTable(Long id);
}
