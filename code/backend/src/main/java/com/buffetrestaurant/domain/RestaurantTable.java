package com.buffetrestaurant.domain;

import com.buffetrestaurant.domain.enums.TableStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true, length = 20)
    private String tableNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TableStatus status = TableStatus.AVAILABLE;

    public RestaurantTable() {
    }

    public RestaurantTable(String tableNumber, Integer capacity) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = TableStatus.AVAILABLE;
    }

    public RestaurantTable(Long id, String tableNumber, Integer capacity, TableStatus status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status != null ? status : TableStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public Boolean isAvailable() {
        return TableStatus.AVAILABLE.equals(this.status);
    }

    public void occupy() {
        this.status = TableStatus.OCCUPIED;
    }

    public void makeAvailable() {
        this.status = TableStatus.AVAILABLE;
    }
}
