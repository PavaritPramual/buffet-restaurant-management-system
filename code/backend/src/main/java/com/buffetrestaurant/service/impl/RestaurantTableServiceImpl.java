package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableStatusRequest;
import com.buffetrestaurant.dto.response.TableResponse;
import com.buffetrestaurant.exception.DuplicateResourceException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.mapper.TableMapper;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.buffetrestaurant.service.RestaurantTableService;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RestaurantTableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository tableRepository;
    private final TableMapper tableMapper;

    public RestaurantTableServiceImpl(RestaurantTableRepository tableRepository, TableMapper tableMapper) {
        this.tableRepository = tableRepository;
        this.tableMapper = tableMapper;
    }

    @Override
    public List<TableResponse> getAllTables(TableStatus status) {
        List<RestaurantTable> tables = (status != null)
                ? tableRepository.findByStatus(status)
                : tableRepository.findAll();
        return tables.stream()
                .map(tableMapper::toResponse)
                .toList();
    }

    @Override
    public TableResponse getTableById(Long id) {
        RestaurantTable table = findTableOrThrow(id);
        return tableMapper.toResponse(table);
    }

    @Override
    @Transactional
    public TableResponse createTable(CreateTableRequest request) {
        if (tableRepository.existsByTableNumber(request.tableNumber())) {
            throw new DuplicateResourceException("Table number '" + request.tableNumber() + "' already exists");
        }

        try {
            RestaurantTable table = tableMapper.toEntity(request);
            RestaurantTable saved = tableRepository.saveAndFlush(table);
            return tableMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Table number '" + request.tableNumber() + "' already exists");
        }
    }

    @Override
    @Transactional
    public TableResponse updateTable(Long id, UpdateTableRequest request) {
        RestaurantTable table = findTableOrThrow(id);

        if (tableRepository.existsByTableNumberAndIdNot(request.tableNumber(), id)) {
            throw new DuplicateResourceException("Table number '" + request.tableNumber() + "' is already in use by another table");
        }

        try {
            table.setTableNumber(request.tableNumber());
            table.setCapacity(request.capacity());

            RestaurantTable updated = tableRepository.saveAndFlush(table);
            return tableMapper.toResponse(updated);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Table number '" + request.tableNumber() + "' is already in use by another table");
        }
    }

    @Override
    @Transactional
    public TableResponse updateTableStatus(Long id, UpdateTableStatusRequest request) {
        RestaurantTable table = findTableOrThrow(id);
        table.setStatus(request.status());
        RestaurantTable updated = tableRepository.save(table);
        return tableMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = findTableOrThrow(id);
        if (TableStatus.OCCUPIED.equals(table.getStatus())) {
            throw new IllegalStateException("Cannot delete table while it is occupied");
        }
        tableRepository.delete(table);
    }

    private RestaurantTable findTableOrThrow(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table not found with id: " + id));
    }
}
