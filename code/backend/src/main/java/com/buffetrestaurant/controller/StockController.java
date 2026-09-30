package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.StockAdjustmentRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.StockItemResponse;
import com.buffetrestaurant.dto.response.StockTransactionResponse;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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

    public StockController(StockService stockService) { this.stockService = stockService; }

    @GetMapping
    public ResponseEntity<List<StockItemResponse>> overview(HttpServletRequest request) {
        if (currentUser(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(stockService.overview());
    }

    @PostMapping("/{itemId}/in")
    public ResponseEntity<StockTransactionResponse> stockIn(@PathVariable Long itemId,
            @Valid @RequestBody StockInRequest body, HttpServletRequest request) {
        UserContext actor = currentUser(request);
        if (actor == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(stockService.stockIn(itemId, body, actor));
    }

    @PostMapping("/{itemId}/adjustments")
    public ResponseEntity<StockTransactionResponse> adjust(@PathVariable Long itemId,
            @Valid @RequestBody StockAdjustmentRequest body, HttpServletRequest request) {
        UserContext actor = currentUser(request);
        if (actor == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(stockService.adjust(itemId, body, actor));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<StockTransactionResponse>> history(
            @RequestParam(name = "itemId", required = false) Long itemId, HttpServletRequest request) {
        if (currentUser(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(stockService.history(itemId));
    }

    private UserContext currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object context = session == null ? null : session.getAttribute(AuthController.USER_CONTEXT_SESSION_KEY);
        return context instanceof UserContext userContext ? userContext : null;
    }
}