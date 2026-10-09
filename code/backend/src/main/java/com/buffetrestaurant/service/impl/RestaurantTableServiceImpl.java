package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.CreateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableRequest;
import com.buffetrestaurant.dto.request.UpdateTableStatusRequest;
import com.buffetrestaurant.dto.response.TableResponse;
import com.buffetrestaurant.exception.DuplicateResourceException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.UserFacingMessages;
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
    private final com.buffetrestaurant.service.MasterDataRemovalAccessProvider removalAccess;

    public RestaurantTableServiceImpl(RestaurantTableRepository tableRepository, TableMapper tableMapper, com.buffetrestaurant.repository.DiningSessionRepository sessions,
            com.buffetrestaurant.service.MasterDataRemovalAccessProvider removalAccess) {
        this.removalAccess = removalAccess;
        this.sessions = sessions;
        this.tableRepository = tableRepository;
        this.tableMapper = tableMapper;
    }

    private final com.buffetrestaurant.repository.DiningSessionRepository sessions;

    private RestaurantTable lockForMaintenance(Long id) {
        RestaurantTable table = tableRepository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Restaurant table not found with id: " + id));
        if (table.isArchived()) throw new DuplicateResourceException("โต๊ะอยู่ในรายการเก็บออก กรุณาคืนรายการก่อนใช้งาน");
        if (sessions.existsByRestaurantTableIdAndStatus(id, com.buffetrestaurant.domain.enums.DiningSessionStatus.ACTIVE)) {
            throw new DuplicateResourceException(UserFacingMessages.TABLE_HAS_ACTIVE_SESSION);
        }
        return table;
    }

    @Override
    public List<TableResponse> getAllTables(TableStatus status) {
        List<RestaurantTable> tables = (status != null)
                ? tableRepository.findByStatus(status)
                : tableRepository.findAll();
        return tables.stream()
                .filter(table -> !table.isArchived())
                .map(tableMapper::toResponse)
                .toList();
    }

    @Override
    public TableResponse getTableById(Long id) {
        RestaurantTable table = findTableOrThrow(id);
        if (table.isArchived()) throw new ResourceNotFoundException("Restaurant table is archived");
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
        RestaurantTable table = lockForMaintenance(id);

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
        RestaurantTable table = lockForMaintenance(id);
        if (request.status() == TableStatus.OCCUPIED) throw new com.buffetrestaurant.exception.DuplicateResourceException("Use open dining session to occupy a table");
        table.setStatus(request.status());
        RestaurantTable updated = tableRepository.save(table);
        return tableMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTable(Long id) {
        removalAccess.requireManagerAccess();
        RestaurantTable table = tableRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table not found with id: " + id));
        if (table.isArchived()) return;
        if (sessions.existsByRestaurantTableIdAndStatus(id, com.buffetrestaurant.domain.enums.DiningSessionStatus.ACTIVE))
            throw new DuplicateResourceException(UserFacingMessages.TABLE_HAS_ACTIVE_SESSION);
        if (TableStatus.OCCUPIED.equals(table.getStatus())) {
            throw new DuplicateResourceException(UserFacingMessages.TABLE_OCCUPIED_CANNOT_DELETE);
        }
        if (sessions.existsByRestaurantTableId(id)) table.archive();
        else tableRepository.delete(table);
        tableRepository.flush();
    }

    @Override
    public List<TableResponse> getArchivedTables() {
        removalAccess.requireManagerAccess();
        return tableRepository.findByArchived(true).stream().map(tableMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public TableResponse restoreTable(Long id) {
        removalAccess.requireManagerAccess();
        RestaurantTable table = tableRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table not found with id: " + id));
        if (table.isArchived()) {
            if (sessions.existsByRestaurantTableIdAndStatus(id, com.buffetrestaurant.domain.enums.DiningSessionStatus.ACTIVE))
                throw new DuplicateResourceException(UserFacingMessages.TABLE_HAS_ACTIVE_SESSION);
            table.restore();
        }
        return tableMapper.toResponse(table);
    }

    private RestaurantTable findTableOrThrow(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant table not found with id: " + id));
    }
}
