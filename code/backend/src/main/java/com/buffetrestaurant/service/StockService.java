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
    private final UserContextProvider access;

    public StockService(StockItemRepository items, StockTransactionRepository transactions,
            @Qualifier("stockInProcessor") StockTransactionProcessor stockInProcessor,
            @Qualifier("stockAdjustmentProcessor") StockTransactionProcessor adjustmentProcessor,
            UserContextProvider access) {
        this.access = access;
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
        return StockItemResponse.from(items.saveAndFlush(new StockItem(request.sku().trim(), request.name().trim(), request.unit().trim(), java.math.BigDecimal.ZERO, request.lowStockThreshold(), targetOrZero(request))));
    }

    @Transactional
    public StockItemResponse updateItem(Long id, @Valid com.buffetrestaurant.dto.request.StockItemRequest request) {
        StockItem item = findItem(id);
        rejectArchived(item);
        if (!item.getUnit().equals(request.unit().trim()) && items.countRecipeReferences(id) > 0) {
            throw new com.buffetrestaurant.exception.ResourceConflictException("เปลี่ยนหน่วยไม่ได้ เพราะมีสูตรเมนูหรือออเดอร์อ้างอิงอยู่");
        }
        if (items.findBySku(request.sku().trim()).filter(other -> !other.getId().equals(id)).isPresent()) {
            throw new com.buffetrestaurant.exception.DuplicateResourceException("Stock SKU already exists");
        }
        if (transactions.existsByStockItemId(id) && (!item.getSku().equals(request.sku().trim()) || !item.getUnit().equals(request.unit().trim()))) {
            throw new com.buffetrestaurant.exception.DuplicateResourceException("SKU and unit cannot change after stock transactions exist");
        }
        item.updateDetails(request.sku().trim(), request.name().trim(), request.unit().trim(), request.lowStockThreshold());
        if (request.openingTargetStock() != null) item.updateOpeningTarget(request.openingTargetStock());
        return StockItemResponse.from(items.saveAndFlush(item));
    }

    @Transactional
    public StockItemResponse setActive(Long id, boolean active) {
        StockItem item = findItem(id);
        rejectArchived(item);
        item.changeActive(active);
        return StockItemResponse.from(items.saveAndFlush(item));
    }

    /**
     * Hard-deletes an item that never had a transaction and holds no stock; otherwise archives it so
     * history and the remaining quantity are kept. Quantity is never edited to make a delete pass.
     */
    @Transactional
    public void removeItem(Long id) {
        requireManager();
        StockItem item = findItem(id);
        if (item.isArchived()) return;
        if (!transactions.existsByStockItemId(id) && items.countRecipeReferences(id) == 0 && item.getQuantity().signum() == 0) {
            items.delete(item);
            items.flush();
        } else {
            item.archive();
            items.saveAndFlush(item);
        }
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> archivedItems() {
        requireManager();
        return items.findAllByArchivedAtIsNotNullOrderByNameAsc().stream().map(StockItemResponse::from).toList();
    }

    @Transactional
    public StockItemResponse restoreItem(Long id) {
        requireManager();
        StockItem item = findItem(id);
        if (item.isArchived()) {
            item.restore();
            items.saveAndFlush(item);
        }
        return StockItemResponse.from(item);
    }

    private void requireManager() {
        access.requireCurrentRequestRole(com.buffetrestaurant.domain.enums.UserRole.MANAGER);
    }

    private static void rejectArchived(StockItem item) {
        if (item.isArchived()) {
            throw new com.buffetrestaurant.exception.ResourceConflictException("รายการนี้ถูกเก็บออกแล้ว กรุณากู้คืนก่อนแก้ไข");
        }
    }

    private static java.math.BigDecimal targetOrZero(com.buffetrestaurant.dto.request.StockItemRequest request) {
        return request.openingTargetStock() == null ? java.math.BigDecimal.ZERO : request.openingTargetStock();
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> overview() {
        return items.findAllByArchivedAtIsNullOrderByNameAsc().stream().map(StockItemResponse::from).toList();
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
