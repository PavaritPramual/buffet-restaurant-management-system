package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.response.ErrorResponse;
import com.buffetrestaurant.dto.response.UserResponse;
import com.buffetrestaurant.domain.enums.UserRole;
import com.buffetrestaurant.service.UserAdministrationService;
import com.buffetrestaurant.service.UserContextProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Staff Profiles")
@SecurityRequirement(name = "staffSessionCookie")
public class AdminUserController {
    private final UserAdministrationService authService;
    private final UserContextProvider users;

    public AdminUserController(UserAdministrationService authService, UserContextProvider users) {
        this.authService = authService;
        this.users = users;
    }

    @GetMapping
    @Operation(summary = "List staff profiles without password credentials")
    @ApiResponse(responseCode = "200", description = "Staff profile list")
    @ApiResponse(responseCode = "401", description = "Staff login required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "MANAGER role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<UserResponse> list(HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        return authService.listUsers();
    }

    @PostMapping
    @Operation(summary = "Create a staff account and profile")
    @ApiResponse(responseCode = "201", description = "Staff profile created; password is never returned")
    @ApiResponse(responseCode = "400", description = "Invalid profile fields or email address",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "MANAGER role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest body, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        UserResponse created = authService.createUser(body);
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.id())).body(created);
    }
}