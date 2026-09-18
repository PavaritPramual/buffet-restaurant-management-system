package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.response.SystemStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/system")
public class SystemController {
    @Operation(summary = "Check that the shared backend foundation is running")
    @GetMapping("/health")
    public ResponseEntity<SystemStatusResponse> health() {
        return ResponseEntity.ok(new SystemStatusResponse("UP", "buffet-restaurant-backend"));
    }
}
