package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.StockAdjustmentRequest;
import com.buffetrestaurant.dto.request.StockInRequest;
import com.buffetrestaurant.dto.response.StockItemResponse;
import com.buffetrestaurant.dto.response.StockTransactionResponse;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.dto.response.ErrorResponse;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.service.SessionUserContextProvider;
import com.buffetrestaurant.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Stock")
@SecurityRequirement(name = "staffSessionCookie")
public class StockController {
    private final StockService stockService;
    private final SessionUserContextProvider users;

    public StockController(StockService stockService, SessionUserContextProvider users) {
        this.stockService = stockService;
        this.users = users;
    }

    @PostMapping("/items")
    @Operation(summary = "Create stock item metadata; new items start at zero quantity")
    @ApiResponse(responseCode = "201", description = "Stock item created")
    @ApiResponse(responseCode = "400", description = "Invalid fields",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required")
    @ApiResponse(responseCode = "403", description = "MANAGER role required")
    @ApiResponse(responseCode = "409", description = "SKU already exists")
    public ResponseEntity<StockItemResponse> createItem(@Valid @RequestBody com.buffetrestaurant.dto.request.StockItemRequest body, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        var item = stockService.createItem(body);
        return ResponseEntity.created(java.net.URI.create("/api/v1/stock/items/" + item.id())).body(item);
    }

    @org.springframework.web.bind.annotation.PutMapping("/items/{id}")
    @Operation(summary = "Update stock item metadata; stock quantity is not changed")
    @ApiResponse(responseCode = "200", description = "Stock item updated")
    @ApiResponse(responseCode = "400", description = "Invalid fields",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required")
    @ApiResponse(responseCode = "403", description = "MANAGER role required")
    @ApiResponse(responseCode = "404", description = "Stock item not found")
    @ApiResponse(responseCode = "409", description = "SKU conflict or item metadata is locked by history")
    public StockItemResponse updateItem(@PathVariable Long id, @Valid @RequestBody com.buffetrestaurant.dto.request.StockItemRequest body, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        return stockService.updateItem(id, body);
    }

    @GetMapping
    @Operation(summary = "List stock items and current quantities")
    @ApiResponse(responseCode = "200", description = "Stock item list")
    @ApiResponse(responseCode = "401", description = "Staff login required")
    @ApiResponse(responseCode = "403", description = "MANAGER or SUPERVISOR role required")
    public List<StockItemResponse> overview(HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return stockService.overview();
    }

    @PostMapping("/{itemId}/in")
    @Operation(summary = "Record stock received")
    @ApiResponse(responseCode = "200", description = "Stock transaction recorded")
    @ApiResponse(responseCode = "400", description = "Invalid quantity, reason or resulting balance",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required")
    @ApiResponse(responseCode = "403", description = "MANAGER or SUPERVISOR role required")
    @ApiResponse(responseCode = "404", description = "Stock item not found")
    public ResponseEntity<StockTransactionResponse> stockIn(@PathVariable Long itemId,
            @Valid @RequestBody StockInRequest body, HttpServletRequest request) {
        UserContext actor = users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return ResponseEntity.ok(stockService.stockIn(itemId, body, actor));
    }

    @PostMapping("/{itemId}/adjustments")
    @Operation(summary = "Record a signed stock quantity adjustment")
    @ApiResponse(responseCode = "200", description = "Stock adjustment recorded")
    @ApiResponse(responseCode = "400", description = "Invalid quantity, reason or resulting balance",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required")
    @ApiResponse(responseCode = "403", description = "MANAGER or SUPERVISOR role required")
    @ApiResponse(responseCode = "404", description = "Stock item not found")
    public ResponseEntity<StockTransactionResponse> adjust(@PathVariable Long itemId,
            @Valid @RequestBody StockAdjustmentRequest body, HttpServletRequest request) {
        UserContext actor = users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return ResponseEntity.ok(stockService.adjust(itemId, body, actor));
    }

    @GetMapping("/transactions")
    @Operation(summary = "List stock transaction history, optionally filtered by item")
    @ApiResponse(responseCode = "200", description = "Stock transaction list")
    @ApiResponse(responseCode = "401", description = "Staff login required")
    @ApiResponse(responseCode = "403", description = "MANAGER or SUPERVISOR role required")
    public List<StockTransactionResponse> history(
            @RequestParam(name = "itemId", required = false) Long itemId, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER, UserRole.SUPERVISOR);
        return stockService.history(itemId);
    }
}