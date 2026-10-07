package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.LoginRequest;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Staff Authentication")
public class AuthController {
    public static final String USER_CONTEXT_SESSION_KEY = "userContext";
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    @Operation(summary = "Log in and establish the staff HTTP session cookie")
    @ApiResponse(responseCode = "200", description = "Authenticated user context; session ID is carried only by Set-Cookie")
    @ApiResponse(responseCode = "400", description = "Missing or invalid request values")
    @ApiResponse(responseCode = "401", description = "Credentials are invalid")
    public ResponseEntity<UserContext> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        UserContext context = authService.authenticate(request.username(), request.password());
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(USER_CONTEXT_SESSION_KEY, context);
        servletRequest.changeSessionId();
        return ResponseEntity.ok(context);
    }

    @GetMapping("/me")
    @Operation(summary = "Read the user context for the current staff session")
    @SecurityRequirement(name = "staffSessionCookie")
    @ApiResponse(responseCode = "200", description = "Current user context")
    @ApiResponse(responseCode = "401", description = "No authenticated session; this endpoint returns an empty body")
    public ResponseEntity<UserContext> current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Object context = session.getAttribute(USER_CONTEXT_SESSION_KEY);
        return context instanceof UserContext userContext
                ? ResponseEntity.ok(userContext)
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate the current staff session")
    @SecurityRequirement(name = "staffSessionCookie")
    @ApiResponse(responseCode = "204", description = "Session invalidated")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.noContent().build();
    }
}