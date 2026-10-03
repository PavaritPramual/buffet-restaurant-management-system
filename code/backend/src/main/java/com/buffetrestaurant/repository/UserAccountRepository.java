package com.buffetrestaurant.repository;

import com.buffetrestaurant.domain.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findAllByOrderByUsernameAsc();
}