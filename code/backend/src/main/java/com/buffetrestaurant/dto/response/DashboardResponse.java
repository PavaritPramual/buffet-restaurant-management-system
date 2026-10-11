package com.buffetrestaurant.dto.response;

import com.buffetrestaurant.domain.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record DashboardResponse(
        LocalDate startDate, LocalDate endDate, String timeZone, OffsetDateTime generatedAt,
        Sales sales, Visits visits, Operations operations, Stock stock, List<PopularItem> popularItems) {
    public record Sales(BigDecimal revenue, long paidBills, BigDecimal averageBill,
                        List<DailySales> daily, List<MethodSales> byMethod) {}
    public record DailySales(LocalDate date, long bills, BigDecimal revenue) {}
    public record MethodSales(PaymentMethod method, long bills, BigDecimal revenue) {}
    public record Visits(long openedSessions, long adults, long children, long orders) {}
    public record Operations(long availableTables, long occupiedTables, long activeSessions,
                             long activeGuests, long unpaidBillRequests, long paidAwaitingClose,
                             long receivedOrders, long preparingOrders, long readyOrders) {}
    public record Stock(long activeItems, long lowStockItems, long outOfStockItems,
                        long belowTargetItems, List<LowStockItem> lowItems) {}
    public record LowStockItem(Long id, String name, String unit, BigDecimal quantity,
                               BigDecimal threshold, BigDecimal target, BigDecimal shortfall) {}
    public record PopularItem(Long menuItemId, String name, long servings) {}
}
