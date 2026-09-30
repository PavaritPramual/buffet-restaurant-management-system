package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.UserAccount;
import com.buffetrestaurant.domain.UserProfile;
import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.dto.response.UserResponse;
import com.buffetrestaurant.exception.InvalidCredentialsException;
import com.buffetrestaurant.repository.UserAccountRepository;
import com.buffetrestaurant.repository.UserProfileRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final UserProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public AuthService(UserAccountRepository users, UserProfileRepository profiles, PasswordEncoder passwordEncoder,
            Validator validator) {
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public UserContext authenticate(String username, String password) {
        UserAccount user = users.findByUsername(username)
                .filter(UserAccount::isActive)
                .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        UserProfile profile = profiles.findByUserId(user.getId()).orElseThrow(InvalidCredentialsException::new);
        return new UserContext(user.getId(), user.getUsername(), profile.getDisplayName(), user.getRole());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        UserAccount user = users.save(new UserAccount(request.username(),
                passwordEncoder.encode(request.password()), request.role()));
        UserProfile profile = profiles.save(new UserProfile(user, request.displayName(), request.email()));
        return new UserResponse(user.getId(), user.getUsername(), profile.getDisplayName(),
                profile.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAllByOrderByUsernameAsc().stream().map(user -> {
            UserProfile profile = profiles.findByUserId(user.getId()).orElse(null);
            return new UserResponse(user.getId(), user.getUsername(),
                    profile == null ? "" : profile.getDisplayName(), profile == null ? null : profile.getEmail(),
                    user.getRole());
        }).toList();
    }
}