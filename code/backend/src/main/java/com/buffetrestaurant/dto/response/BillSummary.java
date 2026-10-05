package com.buffetrestaurant.dto.response;

import java.math.BigDecimal;

/** Stable display breakdown; precise intermediate calculations stay inside BillingEngine. */
public record BillSummary(Long sessionId, BigDecimal subtotalAmount,
        BigDecimal discountAmount, BigDecimal totalAmount) {}
