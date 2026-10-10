package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import com.buffetrestaurant.dto.response.DiningSessionResponse;
import com.buffetrestaurant.dto.response.ErrorResponse;
import com.buffetrestaurant.service.DiningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dining-sessions")
@Tag(name = "Dining Session", description = "Open, look up, and close dining sessions")
@SecurityRequirement(name = "staffSessionCookie")
public class DiningSessionController {
    private final DiningSessionService diningSessionService;

    public DiningSessionController(DiningSessionService diningSessionService) {
        this.diningSessionService = diningSessionService;
    }

    @PostMapping
    @Operation(summary = "Open a dining session for an available table")
    public ResponseEntity<DiningSessionResponse> openSession(
            @Valid @RequestBody OpenDiningSessionRequest request
    ) {
        DiningSessionResponse created = diningSessionService.openSession(request);
        return ResponseEntity.created(URI.create("/api/v1/dining-sessions/" + created.sessionId())).body(created);
    }

    @GetMapping("/active")
    @Operation(summary = "List active dining sessions for staff")
    public ResponseEntity<List<DiningSessionResponse>> getActiveSessions() {
        return ResponseEntity.ok(diningSessionService.getActiveSessions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a dining session for staff")
    public ResponseEntity<DiningSessionResponse> getSession(@PathVariable Long id) {
        return ResponseEntity.ok(diningSessionService.getSession(id));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a dining session after payment is confirmed",
            description = "Requires a staff login cookie and a recorded PAID payment. Closing a session is separate "
                    + "from requesting the bill or recording payment. The staff-only sessionToken is not shown as "
                    + "a value in Swagger examples.")
    @ApiResponse(responseCode = "200", description = "Dining session closed")
    @ApiResponse(responseCode = "400", description = "Session cannot be closed in its current state",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Staff role is not permitted",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Dining session not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<DiningSessionResponse> closeSession(@PathVariable Long id) {
        return ResponseEntity.ok(diningSessionService.closeSession(id));
    }
}
