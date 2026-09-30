package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.response.UserResponse;
import com.buffetrestaurant.service.AuthService;
import com.buffetrestaurant.dto.response.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
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

    public AdminUserController(AuthService authService) { this.authService = authService; }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list(HttpServletRequest request) {
        if (currentUser(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(authService.listUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest body, HttpServletRequest request) {
        if (currentUser(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        UserResponse created = authService.createUser(body);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.id())).body(created);
    }

    private UserContext currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object context = session == null ? null : session.getAttribute(AuthController.USER_CONTEXT_SESSION_KEY);
        return context instanceof UserContext userContext ? userContext : null;
    }
}