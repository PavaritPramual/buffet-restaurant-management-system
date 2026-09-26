package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.BuffetPackage;
import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.RestaurantTable;
import com.buffetrestaurant.domain.Soup;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.PaymentStatus;
import com.buffetrestaurant.domain.enums.TableStatus;
import com.buffetrestaurant.dto.request.OpenDiningSessionRequest;
import com.buffetrestaurant.dto.response.DiningSessionResponse;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.ServiceUnavailableException;
import com.buffetrestaurant.integration.payment.PaymentStatusLookup;
import com.buffetrestaurant.mapper.DiningSessionMapper;
import com.buffetrestaurant.repository.BuffetPackageRepository;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.RestaurantTableRepository;
import com.buffetrestaurant.repository.SoupRepository;
import com.buffetrestaurant.service.DiningSessionService;
import com.buffetrestaurant.service.DiningSessionStaffAccessProvider;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiningSessionServiceImpl implements DiningSessionService {
    private static final int TOKEN_BYTES = 32;

    private final RestaurantTableRepository tableRepository;
    private final BuffetPackageRepository packageRepository;
    private final SoupRepository soupRepository;
    private final DiningSessionRepository diningSessionRepository;
    private final DiningSessionMapper diningSessionMapper;
    private final ObjectProvider<PaymentStatusLookup> paymentStatusLookupProvider;
    private final DiningSessionStaffAccessProvider staffAccessProvider;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Clock clock;

    @Autowired
    public DiningSessionServiceImpl(
            RestaurantTableRepository tableRepository,
            BuffetPackageRepository packageRepository,
            SoupRepository soupRepository,
            DiningSessionRepository diningSessionRepository,
            DiningSessionMapper diningSessionMapper,
            ObjectProvider<PaymentStatusLookup> paymentStatusLookupProvider,
            DiningSessionStaffAccessProvider staffAccessProvider
    ) {
        this(tableRepository, packageRepository, soupRepository, diningSessionRepository,
                diningSessionMapper, paymentStatusLookupProvider, staffAccessProvider, Clock.systemUTC());
    }

    DiningSessionServiceImpl(
            RestaurantTableRepository tableRepository,
            BuffetPackageRepository packageRepository,
            SoupRepository soupRepository,
            DiningSessionRepository diningSessionRepository,
            DiningSessionMapper diningSessionMapper,
            ObjectProvider<PaymentStatusLookup> paymentStatusLookupProvider,
            DiningSessionStaffAccessProvider staffAccessProvider,
            Clock clock
    ) {
        this.tableRepository = tableRepository;
        this.packageRepository = packageRepository;
        this.soupRepository = soupRepository;
        this.diningSessionRepository = diningSessionRepository;
        this.diningSessionMapper = diningSessionMapper;
        this.paymentStatusLookupProvider = paymentStatusLookupProvider;
        this.staffAccessProvider = staffAccessProvider;
        this.clock = clock;
    }

    @Override
    @Transactional
    public DiningSessionResponse openSession(OpenDiningSessionRequest request) {
        staffAccessProvider.requireServiceStaffAccess();
        RestaurantTable table = tableRepository.findByIdForUpdate(request.tableId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant table not found with id: " + request.tableId()));
        if (!TableStatus.AVAILABLE.equals(table.getStatus())) {
            throw new IllegalStateException("Restaurant table is not available");
        }

        int guestCount = request.adultCount() + request.childCount();
        if (guestCount < 1) {
            throw new IllegalStateException("At least one guest is required");
        }
        if (guestCount > table.getCapacity()) {
            throw new IllegalStateException("Guest count exceeds restaurant table capacity");
        }

        BuffetPackage buffetPackage = packageRepository.findById(request.packageId())
                .filter(BuffetPackage::isActive)
                .orElseThrow(() -> new IllegalStateException("Buffet package is not active or does not exist"));
        Soup soup = soupRepository.findById(request.soupId())
                .filter(Soup::isActive)
                .orElseThrow(() -> new IllegalStateException("Soup is not active or does not exist"));

        DiningSession diningSession = new DiningSession(
                table,
                buffetPackage,
                soup,
                request.adultCount(),
                request.childCount(),
                generateToken(),
                LocalDateTime.now(clock)
        );
        table.occupy();
        tableRepository.save(table);
        DiningSession saved = diningSessionRepository.saveAndFlush(diningSession);
        return diningSessionMapper.toResponse(saved);
    }

    @Override
    public List<DiningSessionResponse> getActiveSessions() {
        staffAccessProvider.requireServiceStaffAccess();
        return diningSessionRepository.findByStatusOrderByStartTimeDesc(DiningSessionStatus.ACTIVE).stream()
                .map(diningSessionMapper::toResponse)
                .toList();
    }

    @Override
    public DiningSessionResponse getSession(Long sessionId) {
        staffAccessProvider.requireServiceStaffAccess();
        DiningSession session = diningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Dining session not found with id: " + sessionId));
        return diningSessionMapper.toResponse(session);
    }

    @Override
    public DiningSessionResponse getActiveSessionByToken(String token) {
        DiningSession session = diningSessionRepository
                .findBySessionTokenAndStatus(token, DiningSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
        return diningSessionMapper.toResponse(session);
    }

    @Override
    @Transactional
    public DiningSessionResponse closeSession(Long sessionId) {
        staffAccessProvider.requireServiceStaffAccess();
        DiningSession session = diningSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Dining session not found with id: " + sessionId));
        if (!DiningSessionStatus.ACTIVE.equals(session.getStatus())) {
            throw new IllegalStateException("Dining session is not active");
        }

        PaymentStatusLookup paymentStatusLookup = paymentStatusLookupProvider.getIfAvailable();
        if (paymentStatusLookup == null) {
            throw new ServiceUnavailableException("Payment status verification is not configured");
        }
        PaymentStatusLookup.PaymentVerification verification = paymentStatusLookup.findPaymentForSession(sessionId);
        if (verification == null) {
            throw new IllegalStateException("No payment result is available for this dining session");
        }
        if (!sessionId.equals(verification.sessionId())) {
            throw new IllegalStateException("Payment result belongs to a different dining session");
        }
        if (!PaymentStatus.PAID.equals(verification.status())) {
            throw new IllegalStateException("Dining session can only be closed after payment is PAID");
        }

        RestaurantTable table = tableRepository.findByIdForUpdate(session.getRestaurantTable().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant table not found with id: " + session.getRestaurantTable().getId()));
        session.complete(LocalDateTime.now(clock));
        table.makeAvailable();
        tableRepository.save(table);
        return diningSessionMapper.toResponse(session);
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
