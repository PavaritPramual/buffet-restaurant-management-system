package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.response.UserResponse;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.service.AuthService;
import com.buffetrestaurant.service.SessionUserContextProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {
    private final AuthService authService;
    private final SessionUserContextProvider users;

    public AdminUserController(AuthService authService, SessionUserContextProvider users) {
        this.authService = authService;
        this.users = users;
    }

    @GetMapping
    public List<UserResponse> list(HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        return authService.listUsers();
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest body, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        UserResponse created = authService.createUser(body);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.id())).body(created);
    }
}