package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.dto.response.UserResponse;
import com.buffetrestaurant.exception.ResourceConflictException;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Manager-only removal (hard delete or archive), restore and enable/disable of staff accounts. */
@Service
public class UserLifecycleService {
    public static final String CANNOT_REMOVE_SELF = "ไม่สามารถลบหรือปิดบัญชีของตนเองได้";
    public static final String LAST_MANAGER = "ต้องมีผู้จัดการที่ใช้งานได้อย่างน้อย 1 คน จึงไม่สามารถลบหรือปิดบัญชีนี้ได้";
    public static final String ARCHIVED_ACCOUNT = "บัญชีนี้ถูกเก็บออกแล้ว กรุณากู้คืนก่อนเปิดใช้งาน";

    private final UserAccountRepository users;
    private final UserProfileRepository profiles;
    private final StockTransactionRepository stockTransactions;
    private final StaffSessionRegistry sessions;

    public UserLifecycleService(UserAccountRepository users, UserProfileRepository profiles,
            StockTransactionRepository stockTransactions, StaffSessionRegistry sessions) {
        this.users = users;
        this.profiles = profiles;
        this.stockTransactions = stockTransactions;
        this.sessions = sessions;
    }

    /**
     * Hard-deletes an account that nothing references; otherwise closes and archives it so the
     * actor name on stock history stays readable. Either way every session it holds is revoked.
     */
    @Transactional
    public void remove(Long id, UserContext actor) {
        rejectSelf(id, actor);
        List<UserAccount> activeManagers = users.lockActiveManagers();
        UserAccount target = lockUser(id);
        if (target.isArchived()) return;
        rejectLastManager(target, activeManagers);
        if (stockTransactions.existsByActorId(id)) {
            target.archive();
            users.saveAndFlush(target);
        } else {
            profiles.findByUserId(id).ifPresent(profiles::delete);
            users.delete(target);
            users.flush();
        }
        revokeAfterCommit(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listArchived() {
        return users.findAllByArchivedAtIsNotNullOrderByUsernameAsc().stream()
                .map(user -> AuthService.toResponse(user, profiles.findByUserId(user.getId()).orElse(null)))
                .toList();
    }

    @Transactional
    public UserResponse restore(Long id) {
        UserAccount target = lockUser(id);
        if (target.isArchived()) {
            target.restore();
            users.saveAndFlush(target);
        }
        return response(target);
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active, UserContext actor) {
        if (!active) rejectSelf(id, actor);
        List<UserAccount> activeManagers = users.lockActiveManagers();
        UserAccount target = lockUser(id);
        if (target.isArchived() && active) throw new ResourceConflictException(ARCHIVED_ACCOUNT);
        if (!active && target.isActive()) rejectLastManager(target, activeManagers);
        if (target.isActive() != active) {
            target.changeActive(active);
            users.saveAndFlush(target);
            if (!active) revokeAfterCommit(id);
        }
        return response(target);
    }

    private UserAccount lockUser(Long id) {
        return users.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse response(UserAccount user) {
        return AuthService.toResponse(user, profiles.findByUserId(user.getId()).orElse(null));
    }

    private static void rejectSelf(Long id, UserContext actor) {
        if (actor != null && id.equals(actor.userId())) throw new ResourceConflictException(CANNOT_REMOVE_SELF);
    }

    private static void rejectLastManager(UserAccount target, List<UserAccount> activeManagers) {
        boolean countsAsManager = target.getRole() == UserRole.MANAGER && target.isActive();
        if (countsAsManager && activeManagers.stream().noneMatch(manager -> !manager.getId().equals(target.getId()))) {
            throw new ResourceConflictException(LAST_MANAGER);
        }
    }

    // Revoke only once the change is committed so a failed transaction never logs anyone out.
    private void revokeAfterCommit(Long id) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            sessions.revoke(id);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sessions.revoke(id);
            }
        });
    }
}
