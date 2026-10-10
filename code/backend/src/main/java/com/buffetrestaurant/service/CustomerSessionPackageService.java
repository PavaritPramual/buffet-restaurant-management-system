package com.buffetrestaurant.service;

import com.buffetrestaurant.dto.response.BuffetPackageResponse;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.mapper.CatalogMapper;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reads the package already assigned to an authenticated ACTIVE customer session. */
@Service
@Transactional(readOnly = true)
public class CustomerSessionPackageService {
    private final CustomerSessionVerifier customerAccess;
    private final BuffetPackageRepository packages;
    private final CatalogMapper mapper;

    public CustomerSessionPackageService(CustomerSessionVerifier customerAccess,
                                         BuffetPackageRepository packages, CatalogMapper mapper) {
        this.customerAccess = customerAccess;
        this.packages = packages;
        this.mapper = mapper;
    }

    public BuffetPackageResponse getPackage(Long sessionId, String credential) {
        var session = customerAccess.requireSession(sessionId, credential);
        // Archive stops selection for new sessions, not reads by the existing session.
        var buffetPackage = packages.findById(session.packageId())
                .orElseThrow(() -> new ResourceNotFoundException("Session package not found"));
        return mapper.toResponse(buffetPackage);
    }
}
