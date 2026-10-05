package com.buffetrestaurant.service.billing;

import com.buffetrestaurant.dto.billing.BillingContext;

public interface BillingContextProvider {
    
    BillingContext findBySessionId(Long sessionId);
}
