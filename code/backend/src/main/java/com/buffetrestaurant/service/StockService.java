package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.dto.request.StockAdjustmentRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.StockItemResponse;
import com.buffetrestaurant.dto.response.StockTransactionResponse;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class StockService {
    private final StockItemRepository items;
    private final StockTransactionRepository transactions;
    private final StockTransactionProcessor stockInProcessor;
    private final StockTransactionProcessor adjustmentProcessor;

    public StockService(StockItemRepository items, StockTransactionRepository transactions,
            @Qualifier("stockInProcessor") StockTransactionProcessor stockInProcessor,
            @Qualifier("stockAdjustmentProcessor") StockTransactionProcessor adjustmentProcessor) {
        this.items = items;
        this.transactions = transactions;
        this.stockInProcessor = stockInProcessor;
        this.adjustmentProcessor = adjustmentProcessor;
    }

    @Transactional
    public StockItemResponse createItem(@Valid com.buffetrestaurant.dto.request.StockItemRequest request) {
        if (items.findBySku(request.sku().trim()).isPresent()) {
            throw new com.buffetrestaurant.exception.DuplicateResourceException("Stock SKU already exists");
        }
        return StockItemResponse.from(items.saveAndFlush(new StockItem(request.sku().trim(), request.name().trim(), request.unit().trim(), java.math.BigDecimal.ZERO, request.lowStockThreshold())));
    }

    @Transactional
    public StockItemResponse updateItem(Long id, @Valid com.buffetrestaurant.dto.request.StockItemRequest request) {
        StockItem item = findItem(id);
        if (items.findBySku(request.sku().trim()).filter(other -> !other.getId().equals(id)).isPresent()) {
            throw new com.buffetrestaurant.exception.DuplicateResourceException("Stock SKU already exists");
        }
        if (transactions.existsByStockItemId(id) && (!item.getSku().equals(request.sku().trim()) || !item.getUnit().equals(request.unit().trim()))) {
            throw new com.buffetrestaurant.exception.DuplicateResourceException("SKU and unit cannot change after stock transactions exist");
        }
        item.updateDetails(request.sku().trim(), request.name().trim(), request.unit().trim(), request.lowStockThreshold());
        return StockItemResponse.from(items.saveAndFlush(item));
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> overview() {
        return items.findAllByOrderByNameAsc().stream().map(StockItemResponse::from).toList();
    }

    @Transactional
    public StockTransactionResponse stockIn(Long itemId, @Valid StockInRequest request, UserContext actor) {
        return StockTransactionResponse.from(stockInProcessor.process(findItem(itemId), request.quantity(),
                request.reason(), actor));
    }

    @Transactional
    public StockTransactionResponse adjust(Long itemId, @Valid StockAdjustmentRequest request, UserContext actor) {
        return StockTransactionResponse.from(adjustmentProcessor.process(findItem(itemId), request.quantityDelta(),
                request.reason(), actor));
    }

    @Transactional(readOnly = true)
    public List<StockTransactionResponse> history(Long itemId) {
        List<StockTransaction> history = itemId == null
                ? transactions.findAllByOrderByCreatedAtDescIdDesc()
                : transactions.findAllByStockItemIdOrderByCreatedAtDescIdDesc(itemId);
        return history.stream().map(StockTransactionResponse::from).toList();
    }

    private StockItem findItem(Long itemId) {
        return items.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + itemId));
    }
}
