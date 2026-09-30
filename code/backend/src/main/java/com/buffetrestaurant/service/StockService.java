package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.StockItem;
import com.buffetrestaurant.domain.StockTransaction;
import com.buffetrestaurant.dto.request.StockAdjustmentRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.StockItemResponse;
import com.buffetrestaurant.dto.response.StockTransactionResponse;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {
    private final StockItemRepository items;
    private final StockTransactionRepository transactions;
    private final StockInProcessor stockInProcessor;
    private final StockAdjustmentProcessor adjustmentProcessor;

    public StockService(StockItemRepository items, StockTransactionRepository transactions,
            StockInProcessor stockInProcessor, StockAdjustmentProcessor adjustmentProcessor) {
        this.items = items;
        this.transactions = transactions;
        this.stockInProcessor = stockInProcessor;
        this.adjustmentProcessor = adjustmentProcessor;
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> overview() {
        return items.findAllByOrderByNameAsc().stream().map(StockItemResponse::from).toList();
    }

    @Transactional
    public StockTransactionResponse stockIn(Long itemId, StockInRequest request, UserContext actor) {
        return StockTransactionResponse.from(stockInProcessor.process(findItem(itemId), request.quantity(),
                request.reason(), actor));
    }

    @Transactional
    public StockTransactionResponse adjust(Long itemId, StockAdjustmentRequest request, UserContext actor) {
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
        return items.findById(itemId).orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + itemId));
    }
}