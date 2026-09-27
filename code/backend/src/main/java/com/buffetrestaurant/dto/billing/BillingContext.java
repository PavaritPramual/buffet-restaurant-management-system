package com.buffetrestaurant.dto.billing;

import com.buffetrestaurant.domain.enums.DiningSessionStatus;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class BillingContext {
    
    private Long sessionId;
    private BigDecimal packagePrice;
    private Integer adultCount;
    private Integer childCount;
    private Map<String, Object> discountContext;
    private DiningSessionStatus sessionStatus;
    
    public Long getSessionId() {
        return sessionId;
    }

    public BigDecimal getPackagePrice() {
        return packagePrice;
    }

    public Integer getAdultCount() {
        return adultCount;
    }

    public Integer getChildCount() {
        return childCount;
    }

    public Map<String, Object> getDiscountContext() {
        return discountContext;
    }
    
    public DiningSessionStatus getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setPackagePrice(BigDecimal packagePrice) {
        this.packagePrice = packagePrice;
    }

    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }

    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }

    public void setDiscountContext(Map<String, Object> discountContext) {
        this.discountContext = discountContext;
    }

    public void setSessionStatus(DiningSessionStatus sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

    
}
