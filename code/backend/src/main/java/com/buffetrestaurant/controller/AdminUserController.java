package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.CreateUserRequest;
import com.buffetrestaurant.dto.request.UserActiveRequest;
import com.buffetrestaurant.dto.response.UserContext;
import com.buffetrestaurant.service.UserLifecycleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.buffetrestaurant.dto.request.UpdateUserProfileRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final UserLifecycleService lifecycle;

    public AdminUserController(UserAdministrationService authService, UserContextProvider users,
            UserLifecycleService lifecycle) {
        this.authService = authService;
        this.users = users;
        this.lifecycle = lifecycle;
    }

    @GetMapping("/archived")
    @Operation(summary = "List archived staff accounts")
    @ApiResponse(responseCode = "200", description = "Archived staff list")
    @ApiResponse(responseCode = "403", description = "MANAGER role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public List<UserResponse> archived(HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        return lifecycle.listArchived();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an unreferenced staff account, or close and archive it; revokes its sessions")
    @ApiResponse(responseCode = "204", description = "Account deleted or archived")
    @ApiResponse(responseCode = "403", description = "MANAGER role required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Self removal or last active MANAGER",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> remove(@PathVariable Long id, HttpServletRequest request) {
        UserContext actor = users.requireAnyRole(request, UserRole.MANAGER);
        lifecycle.remove(id, actor);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore an archived account; it stays disabled until enabled")
    public UserResponse restore(@PathVariable Long id, HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        return lifecycle.restore(id);
    }

    @PutMapping("/{id}/active")
    @Operation(summary = "Enable or disable an account; disabling revokes its sessions")
    @ApiResponse(responseCode = "409", description = "Self disable, last active MANAGER, or archived account",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public UserResponse setActive(@PathVariable Long id, @Valid @RequestBody UserActiveRequest body,
            HttpServletRequest request) {
        UserContext actor = users.requireAnyRole(request, UserRole.MANAGER);
        return lifecycle.setActive(id, body.active(), actor);
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

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable Long id, @RequestBody UpdateUserProfileRequest body,
            HttpServletRequest request) {
        users.requireAnyRole(request, UserRole.MANAGER);
        return authService.updateProfile(id, body);
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