package com.buffetrestaurant.controller;

import com.buffetrestaurant.domain.ManagerOperation;
import com.buffetrestaurant.dto.request.ForceOperationRequest;
import com.buffetrestaurant.dto.response.ManagerDiningSessionResponse;
import com.buffetrestaurant.service.ManagerOperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manager")
@Tag(name = "Manager overrides")
@SecurityRequirement(name = "staffSessionCookie")
public class ManagerOperationsController {
    private final ManagerOperationsService service;
    public ManagerOperationsController(ManagerOperationsService service) { this.service = service; }

    @GetMapping("/dining-sessions/active")
    public List<ManagerDiningSessionResponse> activeSessions() { return service.activeSessions(); }

    @GetMapping("/operations")
    public List<ManagerOperation> history() { return service.history(); }

    @PostMapping("/dining-sessions/{id}/force-close")
    @Operation(summary = "Manager force-closes an active session with a required audit reason",
            description = "Cancels the session, revokes customer/QR access and releases the table, including unpaid sessions. Preserves orders and actual payments; does not mark an unpaid bill paid.")
    public ManagerDiningSessionResponse forceClose(@PathVariable Long id, @Valid @RequestBody ForceOperationRequest request) {
        return service.forceClose(id, request.reason());
    }

    @PostMapping("/menu-items/{id}/force-delete")
    @Operation(summary = "Manager removes a menu from the catalog even with order history",
            description = "Requires an audit reason. Removes from all catalog APIs and ordering, while retaining the referenced row and historical order snapshots.")
    public ResponseEntity<Void> forceDeleteMenu(@PathVariable Long id, @Valid @RequestBody ForceOperationRequest request) {
        service.forceDeleteMenu(id, request.reason());
        return ResponseEntity.noContent().build();
    }
}
