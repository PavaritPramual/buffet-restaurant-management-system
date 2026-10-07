package com.buffetrestaurant.service;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import com.buffetrestaurant.domain.enums.PaymentStatus;
import com.buffetrestaurant.domain.enums.CustomerBillStatus;
import java.math.BigDecimal;
import com.buffetrestaurant.dto.response.BillSummary;
import com.buffetrestaurant.dto.response.CustomerBillStatusResponse;
import com.buffetrestaurant.repository.DiningSessionRepository;
import com.buffetrestaurant.repository.PaymentRepository;
import com.buffetrestaurant.service.CustomerSessionVerifier;
import com.buffetrestaurant.service.billing.BillingContextProvider;
import com.buffetrestaurant.service.billing.BillCalculator;
import com.buffetrestaurant.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class CustomerBillingService {
    private final EntityManager entityManager;
    private final CustomerSessionVerifier access;
    private final DiningSessionRepository sessions;
    private final PaymentRepository payments;
    private final BillingContextProvider contextProvider;
    private final BillCalculator engine;
    public CustomerBillingService(CustomerSessionVerifier access, DiningSessionRepository sessions,
            PaymentRepository payments, BillingContextProvider contextProvider, BillCalculator engine,
            EntityManager entityManager) {
        this.access = access;
        this.sessions = sessions;
        this.payments = payments;
        this.contextProvider = contextProvider;
        this.engine = engine;
        this.entityManager = entityManager;
    }
    @Transactional
    public CustomerBillStatusResponse request(Long id, String credential) {
        access.requireSession(id, credential);
        var session = sessions.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
        entityManager.refresh(session, LockModeType.PESSIMISTIC_WRITE);
        // Refresh the cookie check after locking: close may have committed while we waited.
        access.requireSession(id, credential);
        if (session.getStatus() != DiningSessionStatus.ACTIVE) {
            throw new ResourceNotFoundException("Active dining session not found");
        }
        session.requestBill();
        return status(id, credential);
    }
    public CustomerBillStatusResponse status(Long id, String credential) {
        access.requireSession(id, credential);
        var session = sessions.findById(id).orElseThrow(() -> new ResourceNotFoundException("Active dining session not found"));
        var payment = payments.findBySessionId(id);
        var calculation = engine.calculate(contextProvider.findBySessionId(id));
        var subtotal = calculation.subtotalNoneDiscount().setScale(2, RoundingMode.HALF_UP);
        var paid = payment.filter(p -> p.getPaymentStatus() == PaymentStatus.PAID);
        // A recorded payment fixes the final net bill total. It is never an outstanding balance.
        var total = paid.map(p -> p.getAmount()).orElse(calculation.totalAmount());
        var bill = new BillSummary(id, subtotal, subtotal.subtract(total), total);
        CustomerBillStatus status = paid.isPresent() ? CustomerBillStatus.PAID
                : session.getBillRequestedAt() == null ? CustomerBillStatus.NOT_REQUESTED : CustomerBillStatus.REQUESTED;
        var zero = BigDecimal.ZERO.setScale(2);
        return new CustomerBillStatusResponse(id, status, session.getBillRequestedAt(), bill,
                paid.isPresent() ? zero : total, paid.map(p -> p.getAmount()).orElse(zero));
    }
}
