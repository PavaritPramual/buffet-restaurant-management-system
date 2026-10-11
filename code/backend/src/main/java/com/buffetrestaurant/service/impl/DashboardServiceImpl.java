package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.enums.PaymentMethod;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.DashboardResponse;
import com.buffetrestaurant.dto.response.DashboardResponse.*;
import com.buffetrestaurant.repository.DashboardRepository;
import com.buffetrestaurant.service.DashboardService;
import com.buffetrestaurant.service.UserContextProvider;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

@Service
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class DashboardServiceImpl implements DashboardService {
    private static final ZoneId RESTAURANT_ZONE = ZoneId.of("Asia/Bangkok");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final DashboardRepository reports;
    private final UserContextProvider users;

    public DashboardServiceImpl(DashboardRepository reports, UserContextProvider users) {
        this.reports = reports;
        this.users = users;
    }

    @Override
    public DashboardResponse summarize(LocalDate startDate, LocalDate endDate) {
        users.requireCurrentRequestRole(UserRole.MANAGER, UserRole.SUPERVISOR);
        LocalDate today = LocalDate.now(RESTAURANT_ZONE);
        LocalDate start = startDate == null ? today : startDate;
        LocalDate end = endDate == null ? today : endDate;
        if (end.isBefore(start) || end.isAfter(today) || ChronoUnit.DAYS.between(start, end) >= 366) {
            throw new IllegalArgumentException("เลือกช่วงวันที่ตามลำดับ ไม่เกิน 366 วัน และไม่เกินวันนี้");
        }
        OffsetDateTime from = start.atStartOfDay(RESTAURANT_ZONE).toOffsetDateTime();
        OffsetDateTime until = end.plusDays(1).atStartOfDay(RESTAURANT_ZONE).toOffsetDateTime();
        var byDay = reports.dailySales(from, until).stream()
                .collect(Collectors.toMap(DashboardRepository.DailySales::getSalesDate, Function.identity()));
        var daily = new ArrayList<DailySales>();
        BigDecimal revenue = ZERO;
        long bills = 0;
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            var row = byDay.get(day);
            var sale = new DailySales(day, row == null ? 0 : row.getBills(), row == null ? ZERO : row.getRevenue());
            daily.add(sale);
            revenue = revenue.add(sale.revenue());
            bills += sale.bills();
        }
        var methods = reports.methodSales(from, until).stream()
                .collect(Collectors.toMap(DashboardRepository.MethodSales::getMethod, Function.identity()));
        var methodSales = Arrays.stream(PaymentMethod.values()).map(method -> {
            var row = methods.get(method);
            return new MethodSales(method, row == null ? 0 : row.getBills(), row == null ? ZERO : row.getRevenue());
        }).toList();
        // DiningSession uses LocalDateTime stored as UTC by its existing Clock.systemUTC().
        var visits = reports.visits(from.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime(),
                until.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        var tables = counts(reports.tables());
        var queue = counts(reports.queue());
        var sessions = reports.sessions();
        var stock = reports.stock();
        return new DashboardResponse(start, end, RESTAURANT_ZONE.getId(), OffsetDateTime.now(ZoneOffset.UTC),
                new Sales(revenue, bills, bills == 0 ? ZERO : revenue.divide(BigDecimal.valueOf(bills), 2, RoundingMode.HALF_UP), daily, methodSales),
                new Visits(visits.getSessions(), visits.getAdults(), visits.getChildren(), reports.orders(from, until)),
                new Operations(tables.getOrDefault("AVAILABLE", 0L), tables.getOrDefault("OCCUPIED", 0L),
                        sessions.getSessions(), sessions.getGuests(), sessions.getUnpaid(), sessions.getPaid(),
                        queue.getOrDefault("RECEIVED", 0L), queue.getOrDefault("PREPARING", 0L), queue.getOrDefault("READY", 0L)),
                new Stock(stock.getActiveItems(), stock.getLowItems(), stock.getOutOfStockItems(), stock.getBelowTargetItems(),
                        reports.lowStock(PageRequest.of(0, 10)).stream().map(s -> new LowStockItem(s.getId(), s.getName(), s.getUnit(),
                                s.getQuantity(), s.getThreshold(), s.getTarget(), s.getTarget().subtract(s.getQuantity()).max(BigDecimal.ZERO))).toList()),
                reports.popularItems(from, until, PageRequest.of(0, 5)).stream()
                        .map(i -> new PopularItem(i.getId(), i.getName(), i.getServings())).toList());
    }

    private Map<String, Long> counts(java.util.List<DashboardRepository.StatusCount> rows) {
        return rows.stream().collect(Collectors.toMap(DashboardRepository.StatusCount::getStatus, DashboardRepository.StatusCount::getTotal));
    }
}
