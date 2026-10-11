package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.Payment;
import com.buffetrestaurant.domain.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Read models aggregate in the database; no entity graph or mutation privileges are exposed. */
public interface DashboardRepository extends Repository<Payment, Long> {
    interface DailySales {
        int getSalesYear(); int getSalesMonth(); int getSalesDay();
        default LocalDate getSalesDate() { return LocalDate.of(getSalesYear(), getSalesMonth(), getSalesDay()); }
        long getBills(); BigDecimal getRevenue();
    }
    interface MethodSales {
        PaymentMethod getMethod(); long getBills(); BigDecimal getRevenue();
    }
    interface Visits {
        long getSessions(); long getAdults(); long getChildren();
    }
    interface StatusCount { String getStatus(); long getTotal(); }
    interface SessionCounts {
        long getSessions(); long getGuests(); long getUnpaid(); long getPaid();
    }
    interface StockCounts {
        long getActiveItems(); long getLowItems(); long getOutOfStockItems(); long getBelowTargetItems();
    }
    interface LowStock {
        Long getId(); String getName(); String getUnit(); BigDecimal getQuantity();
        BigDecimal getThreshold(); BigDecimal getTarget();
    }
    interface PopularItem { Long getId(); String getName(); long getServings(); }

    // H2 casts a zoned timestamp to DATE in the database session timezone. Extract
    // Bangkok calendar fields explicitly so H2 and PostgreSQL group the same day.
    @Query(value = """
            select extract(year from paid_at at time zone 'Asia/Bangkok') as salesYear,
                   extract(month from paid_at at time zone 'Asia/Bangkok') as salesMonth,
                   extract(day from paid_at at time zone 'Asia/Bangkok') as salesDay,
                   count(*) as bills, sum(amount) as revenue
            from payments where payment_status = 'PAID' and paid_at >= :start and paid_at < :end
            group by extract(year from paid_at at time zone 'Asia/Bangkok'),
                     extract(month from paid_at at time zone 'Asia/Bangkok'),
                     extract(day from paid_at at time zone 'Asia/Bangkok')
            order by salesYear, salesMonth, salesDay
            """, nativeQuery = true)
    List<DailySales> dailySales(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("""
            select p.paymentMethod as method, count(p) as bills, sum(p.amount) as revenue
            from Payment p where p.paymentStatus = com.buffetrestaurant.domain.enums.PaymentStatus.PAID
            and p.paidAt >= :start and p.paidAt < :end group by p.paymentMethod
            """)
    List<MethodSales> methodSales(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("""
            select count(s) as sessions, coalesce(sum(s.adultCount), 0) as adults,
                   coalesce(sum(s.childCount), 0) as children
            from DiningSession s where s.startTime >= :start and s.startTime < :end
            """)
    Visits visits(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select count(o) from CustomerOrder o where o.createdAt >= :start and o.createdAt < :end")
    long orders(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);

    @Query("select t.status as status, count(t) as total from RestaurantTable t where t.archived = false group by t.status")
    List<StatusCount> tables();

    @Query(value = """
            select count(*) as sessions, coalesce(sum(s.adult_count + s.child_count), 0) as guests,
                   coalesce(sum(case when s.bill_requested_at is not null and p.id is null then 1 else 0 end), 0) as unpaid,
                   coalesce(sum(case when p.id is not null then 1 else 0 end), 0) as paid
            from dining_sessions s left join payments p on p.session_id = s.id and p.payment_status = 'PAID'
            where s.status = 'ACTIVE'
            """, nativeQuery = true)
    SessionCounts sessions();

    @Query("""
            select o.status as status, count(o) as total from CustomerOrder o
            where exists (select s.id from DiningSession s where s.id = o.sessionId
                          and s.status = com.buffetrestaurant.domain.enums.DiningSessionStatus.ACTIVE)
            group by o.status
            """)
    List<StatusCount> queue();

    @Query(value = """
            select count(*) as activeItems,
                   coalesce(sum(case when quantity <= low_stock_threshold then 1 else 0 end), 0) as lowItems,
                   coalesce(sum(case when quantity = 0 then 1 else 0 end), 0) as outOfStockItems,
                   coalesce(sum(case when quantity < opening_target_stock then 1 else 0 end), 0) as belowTargetItems
            from stock_items where active = true and archived_at is null
            """, nativeQuery = true)
    StockCounts stock();

    @Query("""
            select s.id as id, s.name as name, s.unit as unit, s.quantity as quantity,
                   s.lowStockThreshold as threshold, s.openingTargetStock as target
            from StockItem s where s.active = true and s.archivedAt is null and s.quantity <= s.lowStockThreshold
            order by s.quantity asc, s.id asc
            """)
    List<LowStock> lowStock(Pageable limit);

    @Query("""
            select i.menuItemId as id, i.itemName as name, sum(i.quantity) as servings from OrderItem i
            where i.order.createdAt >= :start and i.order.createdAt < :end
            group by i.menuItemId, i.itemName order by sum(i.quantity) desc, i.menuItemId asc, i.itemName asc
            """)
    List<PopularItem> popularItems(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end, Pageable limit);
}
