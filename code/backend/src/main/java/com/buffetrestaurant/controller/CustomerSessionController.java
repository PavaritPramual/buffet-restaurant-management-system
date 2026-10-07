package com.buffetrestaurant.controller;

import com.buffetrestaurant.config.CustomerOriginGuard;
import com.buffetrestaurant.dto.request.ExchangeQrRequest;
import com.buffetrestaurant.dto.response.CustomerSessionResponse;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dining-sessions")
public class CustomerSessionController {
    private final CustomerSessionAccessService accessService;
    private final CustomerOriginGuard originGuard;

    @Value("${CUSTOMER_COOKIE_SECURE:false}")
    private boolean cookieSecure;

    @Value("${CUSTOMER_COOKIE_SAME_SITE:Lax}")
    private String cookieSameSite;

    public CustomerSessionController(CustomerSessionAccessService accessService,
                                     CustomerOriginGuard originGuard) {
        this.accessService = accessService;
        this.originGuard = originGuard;
    }

    @PostMapping("/qr-exchange")
    @Operation(summary = "Redeem a single-use QR code for a customer cookie",
            description = "The QR token is accepted only in this POST request body. On success the server sets "
                    + "an HttpOnly customer_session cookie and returns a token-free customer context. "
                    + "No real QR token or cookie value is shown in this API documentation.")
    @ApiResponse(responseCode = "200", description = "Customer context; credential is set as an HttpOnly cookie")
    @ApiResponse(responseCode = "400", description = "Malformed or invalid QR exchange request")
    @ApiResponse(responseCode = "403", description = "Origin is not allowed")
    public ResponseEntity<CustomerSessionResponse> exchange(
            @RequestHeader(name = "Origin", required = false) String origin,
            @Valid @RequestBody ExchangeQrRequest request) {
        originGuard.requireAllowed(origin);
        CustomerSessionAccessService.ExchangeResult result = accessService.exchange(request.token());
        ResponseCookie cookie = ResponseCookie.from(CustomerSessionAccessService.COOKIE_NAME, result.credential())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/api/v1/dining-sessions")
                .maxAge(CustomerSessionAccessService.CREDENTIAL_LIFETIME)
                .build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, cookie.toString()).body(result.session());
    }

    @GetMapping("/customer-context")
    @Operation(summary = "Read the active customer session from its HttpOnly cookie")
    @SecurityRequirement(name = "customerSessionCookie")
    @ApiResponse(responseCode = "200", description = "Customer context without the QR credential")
    @ApiResponse(responseCode = "401", description = "Customer cookie is missing, invalid or expired")
    public ResponseEntity<CustomerSessionResponse> context(
            @CookieValue(name = CustomerSessionAccessService.COOKIE_NAME, required = false) String credential) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(accessService.requireContext(credential));
    }
}
