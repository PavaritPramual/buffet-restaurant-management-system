package com.buffetrestaurant.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buffetrestaurant.domain.Payment;
import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.dto.billing.BillingContext;
import com.buffetrestaurant.dto.request.CreatePaymentRequest;
import com.buffetrestaurant.service.billing.BillCalculation;
import com.buffetrestaurant.dto.response.PaymentResult;
import com.buffetrestaurant.exception.DuplicateResourceException;
import com.buffetrestaurant.repository.PaymentRepository;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import com.buffetrestaurant.service.PaymentService;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import com.buffetrestaurant.service.billing.BillCalculator;
import com.buffetrestaurant.service.PaymentAccessProvider;

@Service
@Transactional 
public class PaymentServiceImpl implements PaymentService{

    private final EntityManager entityManager;
    private final DiningSessionRepository sessions;

    @Override
    @Transactional(readOnly = true)
    public PaymentResult findBySessionId(Long sessionId) {
        accessProvider.requirePaymentAccess();
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("A positive session ID is required");
        }
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new com.buffetrestaurant.exception.ResourceNotFoundException(
                        "Payment not found for this session"));
        return new PaymentResult(payment.getId(), payment.getSessionId(), payment.getAmount(),
                payment.getPaymentMethod(), payment.getPaymentStatus(), payment.getPaidAt());
    }

    private final PaymentRepository paymentRepository;
    private final BillingContextProvider contextProvider;
    private final BillCalculator billingEngine;
    private final PaymentAccessProvider accessProvider;
    
    public PaymentServiceImpl(
        PaymentRepository paymentRepository,
        BillingContextProvider billingContextProvider,
        BillCalculator billingEngine,
        PaymentAccessProvider accessProvider,
        DiningSessionRepository sessions,
        EntityManager entityManager
    ){
        this.paymentRepository = paymentRepository;
        this.contextProvider = billingContextProvider;
        this.billingEngine = billingEngine;
        this.accessProvider = accessProvider;
        this.sessions = sessions;
        this.entityManager = entityManager;

    }

    @Override 
    public PaymentResult pay(CreatePaymentRequest request){
        accessProvider.requirePaymentAccess();

        if (request == null
            || request.sessionId() == null
            || request.sessionId() <= 0
            || request.paymentMethod() == null
        ){
            throw new IllegalArgumentException("Valid Session ID and Patment method are required");
        }

        var session = sessions.findByIdForUpdate(request.sessionId()).orElseThrow(() ->
                new ResourceNotFoundException("Dining session not found"));
        // Re-read after waiting on the same lock used by Order, Bill Request and Close.
        entityManager.refresh(session, LockModeType.PESSIMISTIC_WRITE);
        if (session.getStatus() != DiningSessionStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE status can be paid");
        }
        if (session.getBillRequestedAt() == null) {
            throw new DuplicateResourceException("Request the bill before accepting payment");
        }
        BillingContext context = contextProvider.findBySessionId(request.sessionId());

        if (context == null || !request.sessionId().equals(context.getSessionId())){

            throw new IllegalStateException("Billing context does not match with the requested session");
        }

        if (context.getSessionStatus() != DiningSessionStatus.ACTIVE){

            throw new IllegalStateException("Only ACTIVE status can be paid");   
        }

        if (paymentRepository.findBySessionId(request.sessionId()).isPresent()){

            throw new DuplicateResourceException("A payment already exists for this session");
        }

        BillCalculation summary = billingEngine.calculate(context);
        BigDecimal amount = summary.getTotalAmount();

        if (amount == null || amount.signum() < 0){
            
            throw new IllegalArgumentException("Calculateed payment amount is invalid");
        }

        amount = amount.setScale(2, RoundingMode.UNNECESSARY);

        Payment payment = new Payment(
            request.sessionId(),
            amount,
            request.paymentMethod()
        );

        payment.markPaid(OffsetDateTime.now(ZoneOffset.UTC));

        Payment saved = paymentRepository.saveAndFlush(payment);

        return new PaymentResult(

            saved.getId(),
            saved.getSessionId(),
            saved.getAmount(),
            saved.getPaymentMethod(),
            saved.getPaymentStatus(),
            saved.getPaidAt()
            
        );
    }
}
