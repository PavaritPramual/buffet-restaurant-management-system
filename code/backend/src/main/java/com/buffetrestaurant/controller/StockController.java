package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.StockAdjustmentRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.StockItemResponse;
import com.buffetrestaurant.dto.response.StockTransactionResponse;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.service.SessionUserContextProvider;
import com.buffetrestaurant.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {
    private final StockService stockService;
    private final SessionUserContextProvider users;

    public StockController(StockService stockService, SessionUserContextProvider users) {
        this.stockService = stockService;
        this.users = users;
    }

    @GetMapping
    public List<StockItemResponse> overview(HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return stockService.overview();
    }

    @PostMapping("/{itemId}/in")
    public ResponseEntity<StockTransactionResponse> stockIn(@PathVariable Long itemId,
            @Valid @RequestBody StockInRequest body, HttpServletRequest request) {
        UserContext actor = users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return ResponseEntity.ok(stockService.stockIn(itemId, body, actor));
    }

    @PostMapping("/{itemId}/adjustments")
    public ResponseEntity<StockTransactionResponse> adjust(@PathVariable Long itemId,
            @Valid @RequestBody StockAdjustmentRequest body, HttpServletRequest request) {
        UserContext actor = users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return ResponseEntity.ok(stockService.adjust(itemId, body, actor));
    }

    @GetMapping("/transactions")
    public List<StockTransactionResponse> history(
            @RequestParam(name = "itemId", required = false) Long itemId, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return stockService.history(itemId);
    }
}