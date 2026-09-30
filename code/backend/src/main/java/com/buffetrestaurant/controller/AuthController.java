package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.LoginRequest;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public static final String USER_CONTEXT_SESSION_KEY = "userContext";
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<UserContext> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        UserContext context = authService.authenticate(request.username(), request.password());
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(USER_CONTEXT_SESSION_KEY, context);
        return ResponseEntity.ok(context);
    }

    @GetMapping("/me")
    public ResponseEntity<UserContext> current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Object context = session.getAttribute(USER_CONTEXT_SESSION_KEY);
        return context instanceof UserContext userContext
                ? ResponseEntity.ok(userContext)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.noContent().build();
    }
}