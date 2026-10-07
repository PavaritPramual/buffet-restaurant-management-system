package com.buffetrestaurant.service.impl;

import com.buffetrestaurant.domain.CustomerSessionGrant;
import com.buffetrestaurant.domain.DiningSession;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.dto.response.CustomerSessionResponse;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import com.buffetrestaurant.exception.UnauthorizedException;
import com.buffetrestaurant.repository.CustomerSessionGrantRepository;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.service.SessionContextProvider.SessionContextSnapshot;
import com.buffetrestaurant.service.CustomerSessionVerifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerSessionAccessService implements CustomerSessionVerifier {
    public static final String COOKIE_NAME = "customer_session";
    public static final Duration CREDENTIAL_LIFETIME = Duration.ofHours(8);
    private final DiningSessionRepository sessionRepository;
    private final CustomerSessionGrantRepository grantRepository;
    private final EntityManager entityManager;
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    @Autowired
    public CustomerSessionAccessService(DiningSessionRepository sessionRepository,
                                        CustomerSessionGrantRepository grantRepository,
                                        EntityManager entityManager) {
        this(sessionRepository, grantRepository, entityManager, Clock.systemUTC());
    }

    CustomerSessionAccessService(DiningSessionRepository sessionRepository,
                                 CustomerSessionGrantRepository grantRepository,
                                 EntityManager entityManager, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.grantRepository = grantRepository;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Transactional
    public ExchangeResult exchange(String qrToken) {
        if (qrToken == null || qrToken.isBlank()) {
            throw new ResourceNotFoundException("Active QR code not found");
        }
        DiningSession session = sessionRepository.findBySessionTokenForUpdate(qrToken)
                .filter(value -> value.getStatus() == DiningSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active QR code not found"));
        String credential = randomToken();
        OffsetDateTime createdAt = OffsetDateTime.now(clock);
        grantRepository.save(new CustomerSessionGrant(session, hash(credential),
                createdAt, createdAt.plus(CREDENTIAL_LIFETIME)));
        session.rotateQrToken(randomToken());
        sessionRepository.flush();
        return new ExchangeResult(toResponse(session), credential);
    }

    public CustomerSessionResponse requireContext(String credential) {
        return toResponse(requireActiveGrant(credential).getDiningSession());
    }

    public SessionContextSnapshot requireSession(Long sessionId, String credential, boolean forOrder) {
        CustomerSessionGrant grant = requireActiveGrant(credential);
        if (!grant.getDiningSession().getId().equals(sessionId)) {
            throw new ResourceNotFoundException("Active dining session not found");
        }
        DiningSession session = grant.getDiningSession();
        if (forOrder) {
            // Refresh under the same row lock used by closeSession: a grant may have loaded
            // this entity before a concurrent close committed.
            entityManager.refresh(session, LockModeType.PESSIMISTIC_WRITE);
        }
        if (session.getStatus() != DiningSessionStatus.ACTIVE) {
            throw new ResourceNotFoundException("Active dining session not found");
        }
        if (forOrder && session.getBillRequestedAt() != null) {
            throw new com.buffetrestaurant.exception.DuplicateResourceException("This session has requested its bill and no longer accepts orders");
        }
        return new SessionContextSnapshot(session.getId(), session.getBuffetPackage().getId(),
                session.getRestaurantTable().getTableNumber(), session.getStatus());
    }

    @Override
    public SessionContextSnapshot requireSession(Long sessionId, String credential) {
        return requireSession(sessionId, credential, false);
    }

    @Override
    public SessionContextSnapshot requireSessionForOrder(Long sessionId, String credential) {
        return requireSession(sessionId, credential, true);
    }

    private CustomerSessionGrant requireActiveGrant(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new UnauthorizedException("Customer session is missing");
        }
        CustomerSessionGrant grant = grantRepository.findByTokenHash(hash(credential))
                .orElseThrow(() -> new UnauthorizedException("Customer session is invalid"));
        if (!grant.getExpiresAt().isAfter(OffsetDateTime.now(clock))) {
            throw new UnauthorizedException("Customer session has expired");
        }
        if (grant.getDiningSession().getStatus() != DiningSessionStatus.ACTIVE) {
            throw new ResourceNotFoundException("Active dining session not found");
        }
        return grant;
    }

    private CustomerSessionResponse toResponse(DiningSession session) {
        return new CustomerSessionResponse(session.getId(), session.getBuffetPackage().getId(),
                session.getRestaurantTable().getTableNumber(), session.getStatus());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String credential) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(credential.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record ExchangeResult(CustomerSessionResponse session, String credential) {
    }
}
