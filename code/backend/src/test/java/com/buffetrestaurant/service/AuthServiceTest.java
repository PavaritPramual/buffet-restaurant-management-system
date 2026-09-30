package com.buffetrestaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.UserProfile;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.exception.InvalidCredentialsException;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import java.util.Optional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.validation.Validation;

class AuthServiceTest {
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final UserProfileRepository profiles = mock(UserProfileRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuthService service = new AuthService(users, profiles, encoder,
            Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void authenticatesWithBcryptAndReturnsRoleContext() {
        UserAccount user = new UserAccount("manager", encoder.encode("correct-horse"), UserRole.MANAGER);
        user.setId(7L);
        UserProfile profile = new UserProfile(user, "Restaurant Manager", "manager@example.test");
        when(users.findByUsername("manager")).thenReturn(Optional.of(user));
        when(profiles.findByUserId(7L)).thenReturn(Optional.of(profile));

        var context = service.authenticate("manager", "correct-horse");

        assertEquals(7L, context.userId());
        assertEquals(UserRole.MANAGER, context.role());
        assertEquals("Restaurant Manager", context.displayName());
    }

    @Test
    void rejectsIncorrectPassword() {
        UserAccount user = new UserAccount("manager", encoder.encode("correct-horse"), UserRole.MANAGER);
        when(users.findByUsername("manager")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> service.authenticate("manager", "wrong-password"));
    }

    @Test
    void validatesCreateUserRequestBeforeSaving() {
        CreateUserRequest invalid = new CreateUserRequest("", "short", "", null, UserRole.MANAGER);

        assertThrows(ConstraintViolationException.class, () -> service.createUser(invalid));
        verify(users, never()).save(any(UserAccount.class));
        verify(profiles, never()).save(any(UserProfile.class));
    }
}