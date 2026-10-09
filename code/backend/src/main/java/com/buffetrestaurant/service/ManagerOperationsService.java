package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.ManagerOperation;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.ManagerDiningSessionResponse;
import com.buffetrestaurant.exception.BusinessRuleException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ManagerOperationsService {
    private final UserContextProvider users;
    private final DiningSessionRepository sessions;
    private final RestaurantTableRepository tables;
    private final CustomerSessionGrantRepository grants;
    private final MenuItemRepository menus;
    private final ManagerOperationRepository audit;
    private final EntityManager entities;

    public ManagerOperationsService(UserContextProvider users, DiningSessionRepository sessions,
            RestaurantTableRepository tables, CustomerSessionGrantRepository grants,
            MenuItemRepository menus, ManagerOperationRepository audit, EntityManager entities) {
        this.users = users; this.sessions = sessions; this.tables = tables; this.grants = grants;
        this.menus = menus; this.audit = audit; this.entities = entities;
    }

    public List<ManagerDiningSessionResponse> activeSessions() {
        users.requireCurrentRequestRole(UserRole.MANAGER);
        return sessions.findByStatusOrderByStartTimeDesc(DiningSessionStatus.ACTIVE).stream()
                .map(ManagerDiningSessionResponse::from).toList();
    }

    public List<ManagerOperation> history() {
        users.requireCurrentRequestRole(UserRole.MANAGER);
        return audit.findTop50ByOrderByIdDesc();
    }

    @Transactional
    public ManagerDiningSessionResponse forceClose(Long id, String rawReason) {
        var actor = users.requireCurrentRequestRole(UserRole.MANAGER);
        String reason = reason(rawReason);
        var session = sessions.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบรอบกินนี้"));
        // Same session lock as Order, Bill Request, Payment and normal Close.
        entities.refresh(session, LockModeType.PESSIMISTIC_WRITE);
        if (session.getStatus() != DiningSessionStatus.ACTIVE) {
            throw new BusinessRuleException("รอบกินนี้ปิดแล้ว กรุณาโหลดข้อมูลใหม่");
        }
        var table = tables.findByIdForUpdate(session.getRestaurantTable().getId())
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโต๊ะนี้"));
        grants.deleteByDiningSessionId(id);
        session.forceClose(LocalDateTime.now(ZoneOffset.UTC));
        table.makeAvailable();
        audit.save(new ManagerOperation("FORCE_CLOSE_SESSION", id, table.getTableNumber(), reason, actor));
        // Never insert/change a payment: cancellation is not a recorded payment.
        return ManagerDiningSessionResponse.from(session);
    }

    @Transactional
    public void forceDeleteMenu(Long id, String rawReason) {
        var actor = users.requireCurrentRequestRole(UserRole.MANAGER);
        String reason = reason(rawReason);
        var menu = menus.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบเมนูนี้ หรือถูกลบออกแล้ว"));
        entities.refresh(menu, LockModeType.PESSIMISTIC_WRITE);
        if (menu.getDeletedAt() != null) throw new ResourceNotFoundException("เมนูนี้ถูกลบออกแล้ว");
        menu.removeFromCatalog(OffsetDateTime.now(ZoneOffset.UTC));
        audit.save(new ManagerOperation("FORCE_DELETE_MENU", id, menu.getName(), reason, actor));
    }

    private String reason(String value) {
        if (value == null || value.isBlank() || value.length() > 500) {
            throw new BusinessRuleException("กรุณาระบุเหตุผลไม่เกิน 500 ตัวอักษร");
        }
        return value.trim();
    }
}
