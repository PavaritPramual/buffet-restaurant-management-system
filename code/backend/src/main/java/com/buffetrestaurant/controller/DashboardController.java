package com.buffetrestaurant.controller;

import com.buffetrestaurant.common.ApiPaths;
import com.buffetrestaurant.dto.response.DashboardResponse;
import com.buffetrestaurant.dto.response.ErrorResponse;
import com.buffetrestaurant.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.API_V1 + "/dashboard")
@Tag(name = "Dashboard")
@SecurityRequirement(name = "staffSessionCookie")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }

    @GetMapping
    @Operation(summary = "Read management summary (MANAGER or SUPERVISOR)",
            description = "Inclusive Bangkok dates, default today, maximum 366 days. Revenue uses PAID amounts by paid_at. Operations and stock are current snapshots; no billing or fulfillment permissions are granted.")
    @ApiResponse(responseCode = "200", description = "Dashboard summary")
    @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Staff login required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "MANAGER or SUPERVISOR required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<DashboardResponse> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(dashboard.summarize(startDate, endDate));
    }
}
