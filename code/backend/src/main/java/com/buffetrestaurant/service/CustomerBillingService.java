package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.PaymentStatus;
import com.buffetrestaurant.dto.response.BillSummary;
import com.buffetrestaurant.dto.response.CustomerBillStatusResponse;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.PaymentRepository;
import com.buffetrestaurant.service.impl.CustomerSessionAccessService;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import com.buffetrestaurant.service.billing.BillingEngine;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class CustomerBillingService {
    @PersistenceContext
    private EntityManager entityManager;
    private final CustomerSessionAccessService access;
    private final DiningSessionRepository sessions;
    private final PaymentRepository payments;
    private final BillingContextProvider contextProvider;
    private final BillingEngine engine;
    public CustomerBillingService(CustomerSessionAccessService access, DiningSessionRepository sessions,
            PaymentRepository payments, BillingContextProvider contextProvider, BillingEngine engine) {
        this.access = access;
        this.sessions = sessions;
        this.payments = payments;
        this.contextProvider = contextProvider;
        this.engine = engine;
    }
    @Transactional
    public CustomerBillStatusResponse request(Long id, String credential) {
        access.requireSession(id, credential, false);
        var session = sessions.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
        entityManager.refresh(session, LockModeType.PESSIMISTIC_WRITE);
        // Refresh the cookie check after locking: close may have committed while we waited.
        access.requireSession(id, credential, false);
        if (session.getStatus() != DiningSessionStatus.ACTIVE) {
            throw new ResourceNotFoundException("Active dining session not found");
        }
        session.requestBill();
        return status(id, credential);
    }
    public CustomerBillStatusResponse status(Long id, String credential) {
        access.requireSession(id, credential, false);
        var session = sessions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
        var payment = payments.findBySessionId(id);
        var calculation = engine.calculate(contextProvider.findBySessionId(id));
        var subtotal = calculation.subtotalNoneDiscount().setScale(2, RoundingMode.HALF_UP);
        var total = payment.filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(p -> p.getAmount()).orElse(calculation.totalAmount());
        var bill = new BillSummary(id, subtotal, subtotal.subtract(total), total);
        String status = payment.filter(p -> p.getPaymentStatus() == PaymentStatus.PAID).isPresent() ? "PAID"
                : session.getBillRequestedAt() == null ? "NOT_REQUESTED" : "REQUESTED";
        return new CustomerBillStatusResponse(id, status, session.getBillRequestedAt(), bill);
    }
}
