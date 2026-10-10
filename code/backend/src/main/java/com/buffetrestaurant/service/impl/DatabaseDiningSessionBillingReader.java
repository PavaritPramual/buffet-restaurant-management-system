package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.integration.billing.DiningSessionBillingReader;
import com.buffetrestaurant.repository.DiningSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DatabaseDiningSessionBillingReader implements DiningSessionBillingReader {
    private final DiningSessionRepository diningSessionRepository;

    public DatabaseDiningSessionBillingReader(DiningSessionRepository diningSessionRepository) {
        this.diningSessionRepository = diningSessionRepository;
    }

    @Override
    public BillingSnapshot requireBySessionId(Long sessionId) {
        DiningSession session = diningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dining session not found with id: " + sessionId));
        return new BillingSnapshot(
                session.getId(),
                session.getPackagePriceAtOpen(),
                session.getAdultCount(),
                session.getChildCount(),
                session.getStatus()
        );
    }
}
