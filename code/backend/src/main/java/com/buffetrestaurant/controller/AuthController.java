package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.LoginRequest;
import com.buffetrestaurant.dto.response.ErrorResponse;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.AuthenticationService;
import com.buffetrestaurant.service.StaffSessionRegistry;
import com.buffetrestaurant.exception.InvalidCredentialsException;
import com.buffetrestaurant.service.UserContextProvider;
import com.buffetrestaurant.common.UserSessionKeys;
import com.buffetrestaurant.exception.AuthenticationRequiredException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final AuthenticationService authService;
    private final UserContextProvider users;
    private final StaffSessionRegistry sessions;

    public AuthController(AuthenticationService authService, UserContextProvider users,
            StaffSessionRegistry sessions) {
        this.authService = authService;
        this.users = users;
        this.sessions = sessions;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and establish the staff HTTP session cookie")
    @ApiResponse(responseCode = "200", description = "Authenticated user context; session ID is carried only by Set-Cookie")
    @ApiResponse(responseCode = "400", description = "Missing or invalid request values",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credentials are invalid",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserContext> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        UserContext context = authService.authenticate(request.username(), request.password());
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(UserSessionKeys.USER_CONTEXT_SESSION_KEY, context);
        servletRequest.changeSessionId();
        sessions.register(context.userId(), session);
        if (!authService.isLoginAllowed(context.userId())) {
            // The account was closed between the credential check and session registration.
            session.invalidate();
            throw new InvalidCredentialsException();
        }
        return ResponseEntity.ok(context);
    }

    @GetMapping("/me")
    @Operation(summary = "Read the user context for the current staff session")
    @SecurityRequirement(name = "staffSessionCookie")
    @ApiResponse(responseCode = "200", description = "Current user context")
    @ApiResponse(responseCode = "401", description = "No authenticated session; this endpoint returns an empty body",
            content = @Content)
    public ResponseEntity<UserContext> current(HttpServletRequest request) {
        try {
            return ResponseEntity.ok(users.requireAuthenticated(request));
        } catch (AuthenticationRequiredException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
