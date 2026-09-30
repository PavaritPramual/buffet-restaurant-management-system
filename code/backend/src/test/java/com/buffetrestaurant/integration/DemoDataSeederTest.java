package com.buffetrestaurant.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.buffetrestaurant.repository.StockItemRepository;
import com.buffetrestaurant.repository.StockTransactionRepository;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("demo")
class DemoDataSeederTest {
    @Autowired private List<ApplicationRunner> runners;
    @Autowired private UserAccountRepository users;
    @Autowired private UserProfileRepository profiles;
    @Autowired private StockItemRepository items;
    @Autowired private StockTransactionRepository transactions;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void rerunningDemoSeedDoesNotDuplicateUsersItemsOrInitialAudit() throws Exception {
        long initialUsers = users.count();
        long initialProfiles = profiles.count();
        long initialItems = items.count();
        long initialTransactions = transactions.count();
        ApplicationArguments arguments = new DefaultApplicationArguments(new String[0]);
        runners.forEach(runner -> run(runner, arguments));

        assertThat(initialUsers).isEqualTo(1);
        assertThat(initialProfiles).isEqualTo(1);
        assertThat(initialItems).isEqualTo(3);
        assertThat(initialTransactions).isEqualTo(3);
        assertThat(users.count()).isEqualTo(initialUsers);
        assertThat(profiles.count()).isEqualTo(initialProfiles);
        assertThat(items.count()).isEqualTo(initialItems);
        assertThat(transactions.count()).isEqualTo(initialTransactions);
        assertThat(passwordEncoder.matches("admin123", users.findByUsername("admin").orElseThrow().getPasswordHash()))
                .isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM stock_transactions WHERE reason = 'Initial demo stock' AND actor_user_id = (SELECT id FROM app_users WHERE username = 'admin')",
                Integer.class)).isEqualTo(3);
    }

    private void run(ApplicationRunner runner, ApplicationArguments arguments) {
        try {
            runner.run(arguments);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}