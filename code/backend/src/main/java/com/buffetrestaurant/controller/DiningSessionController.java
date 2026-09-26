package com.buffetrestaurant.controller;

import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import com.buffetrestaurant.dto.response.DiningSessionResponse;
import com.buffetrestaurant.service.DiningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
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

    @GetMapping("/token/{token}")
    @Operation(summary = "Look up an active dining session by its QR token")
    public ResponseEntity<DiningSessionResponse> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(diningSessionService.getActiveSessionByToken(token));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close a dining session after payment is confirmed")
    public ResponseEntity<DiningSessionResponse> closeSession(@PathVariable Long id) {
        return ResponseEntity.ok(diningSessionService.closeSession(id));
    }
}
