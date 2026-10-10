package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findAllByOrderByUsernameAsc();
    List<UserAccount> findAllByArchivedAtIsNullOrderByUsernameAsc();
    List<UserAccount> findAllByArchivedAtIsNotNullOrderByUsernameAsc();

    /** Locks every login-capable Manager in id order so concurrent removals cannot both pass the last-Manager check. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.role = com.buffetrestaurant.domain.enums.UserRole.MANAGER "
            + "and u.active = true and u.archivedAt is null order by u.id")
    List<UserAccount> lockActiveManagers();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserAccount u where u.id = :id")
    Optional<UserAccount> findByIdForUpdate(@Param("id") Long id);
}