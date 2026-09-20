package com.buffetrestaurant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.buffetrestaurant.service.impl.RestaurantTableServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestaurantTableServiceTest {

    @Mock
    private RestaurantTableRepository tableRepository;

    private TableMapper tableMapper;
    private RestaurantTableService tableService;

    @BeforeEach
    void setUp() {
        tableMapper = new TableMapper();
        tableService = new RestaurantTableServiceImpl(tableRepository, tableMapper);
    }

    @Test
    void getAllTables_whenStatusNull_returnsAllTables() {
        // Given
        RestaurantTable table1 = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        RestaurantTable table2 = new RestaurantTable(2L, "T02", 2, TableStatus.OCCUPIED);
        when(tableRepository.findAll()).thenReturn(List.of(table1, table2));

        // When
        List<TableResponse> result = tableService.getAllTables(null);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).tableNumber()).isEqualTo("T01");
        assertThat(result.get(1).tableNumber()).isEqualTo("T02");
        verify(tableRepository).findAll();
    }

    @Test
    void getAllTables_whenStatusProvided_returnsFilteredTables() {
        // Given
        RestaurantTable table1 = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.findByStatus(TableStatus.AVAILABLE)).thenReturn(List.of(table1));

        // When
        List<TableResponse> result = tableService.getAllTables(TableStatus.AVAILABLE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(TableStatus.AVAILABLE);
        verify(tableRepository).findByStatus(TableStatus.AVAILABLE);
    }

    @Test
    void getTableById_whenTableExists_returnsTableResponse() {
        // Given
        RestaurantTable table = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        // When
        TableResponse result = tableService.getTableById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.tableNumber()).isEqualTo("T01");
        assertThat(result.capacity()).isEqualTo(4);
        assertThat(result.status()).isEqualTo(TableStatus.AVAILABLE);
    }

    @Test
    void getTableById_whenTableDoesNotExist_throwsResourceNotFoundException() {
        // Given
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tableService.getTableById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant table not found with id: 999");
    }

    @Test
    void createTable_whenTableNumberIsUnique_createsAndReturnsTableResponse() {
        // Given
        CreateTableRequest request = new CreateTableRequest("T01", 4);
        when(tableRepository.existsByTableNumber("T01")).thenReturn(false);

        RestaurantTable saved = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.save(any(RestaurantTable.class))).thenReturn(saved);

        // When
        TableResponse result = tableService.createTable(request);

        // Then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.tableNumber()).isEqualTo("T01");
        assertThat(result.capacity()).isEqualTo(4);
        assertThat(result.status()).isEqualTo(TableStatus.AVAILABLE);
        verify(tableRepository).save(any(RestaurantTable.class));
    }

    @Test
    void createTable_whenTableNumberAlreadyExists_throwsDuplicateResourceException() {
        // Given
        CreateTableRequest request = new CreateTableRequest("T01", 4);
        when(tableRepository.existsByTableNumber("T01")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> tableService.createTable(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Table number 'T01' already exists");
        verify(tableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    void updateTable_whenTableExistsAndNumberNotDuplicated_updatesAndReturnsTableResponse() {
        // Given
        RestaurantTable existing = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tableRepository.existsByTableNumberAndIdNot("T01-NEW", 1L)).thenReturn(false);

        RestaurantTable updated = new RestaurantTable(1L, "T01-NEW", 6, TableStatus.AVAILABLE);
        when(tableRepository.save(existing)).thenReturn(updated);

        UpdateTableRequest request = new UpdateTableRequest("T01-NEW", 6);

        // When
        TableResponse result = tableService.updateTable(1L, request);

        // Then
        assertThat(result.tableNumber()).isEqualTo("T01-NEW");
        assertThat(result.capacity()).isEqualTo(6);
        verify(tableRepository).save(existing);
    }

    @Test
    void updateTable_whenTableDoesNotExist_throwsResourceNotFoundException() {
        // Given
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());
        UpdateTableRequest request = new UpdateTableRequest("T99", 4);

        // When & Then
        assertThatThrownBy(() -> tableService.updateTable(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant table not found with id: 999");
    }

    @Test
    void updateTable_whenTableNumberDuplicatedOnAnotherTable_throwsDuplicateResourceException() {
        // Given
        RestaurantTable existing = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tableRepository.existsByTableNumberAndIdNot("T02", 1L)).thenReturn(true);

        UpdateTableRequest request = new UpdateTableRequest("T02", 6);

        // When & Then
        assertThatThrownBy(() -> tableService.updateTable(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Table number 'T02' is already in use by another table");
        verify(tableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    void updateTableStatus_whenTableExists_updatesStatusAndReturnsTableResponse() {
        // Given
        RestaurantTable existing = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(existing));

        RestaurantTable updated = new RestaurantTable(1L, "T01", 4, TableStatus.OCCUPIED);
        when(tableRepository.save(existing)).thenReturn(updated);

        UpdateTableStatusRequest request = new UpdateTableStatusRequest(TableStatus.OCCUPIED);

        // When
        TableResponse result = tableService.updateTableStatus(1L, request);

        // Then
        assertThat(result.status()).isEqualTo(TableStatus.OCCUPIED);
        verify(tableRepository).save(existing);
    }

    @Test
    void updateTableStatus_whenTableDoesNotExist_throwsResourceNotFoundException() {
        // Given
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());
        UpdateTableStatusRequest request = new UpdateTableStatusRequest(TableStatus.OCCUPIED);

        // When & Then
        assertThatThrownBy(() -> tableService.updateTableStatus(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant table not found with id: 999");
    }

    @Test
    void deleteTable_whenTableIsAvailable_deletesSuccessfully() {
        // Given
        RestaurantTable table = new RestaurantTable(1L, "T01", 4, TableStatus.AVAILABLE);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        // When
        tableService.deleteTable(1L);

        // Then
        verify(tableRepository).delete(table);
    }

    @Test
    void deleteTable_whenTableIsOccupied_throwsIllegalStateException() {
        // Given
        RestaurantTable table = new RestaurantTable(1L, "T01", 4, TableStatus.OCCUPIED);
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        // When & Then
        assertThatThrownBy(() -> tableService.deleteTable(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot delete table while it is occupied");
        verify(tableRepository, never()).delete(any(RestaurantTable.class));
    }

    @Test
    void deleteTable_whenTableDoesNotExist_throwsResourceNotFoundException() {
        // Given
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tableService.deleteTable(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant table not found with id: 999");
    }
}
