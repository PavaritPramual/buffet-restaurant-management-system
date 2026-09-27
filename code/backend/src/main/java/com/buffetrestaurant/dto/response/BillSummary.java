package com.buffetrestaurant.dto.response;

import java.math.BigDecimal;

public class BillSummary {
    
    private Long sessionId;
    private BigDecimal subtotalNoneDiscount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal totalBeforeRounding;
    private BigDecimal roundingAdjustment;
    
    public Long getSessionId() {
        return sessionId;
    }

    public BigDecimal getSubtotalNoneDiscount() {
        return subtotalNoneDiscount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setSubtotalNoneDiscount(BigDecimal subtotalNoneDiscount) {
        this.subtotalNoneDiscount = subtotalNoneDiscount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalBeforeRounding() {
        return totalBeforeRounding;
    }

    public BigDecimal getRoundingAdjustment() {
        return roundingAdjustment;
    }

    public void setTotalBeforeRounding(BigDecimal totalBeforeRounding) {
        this.totalBeforeRounding = totalBeforeRounding;
    }

    public void setRoundingAdjustment(BigDecimal roundingAdjustment) {
        this.roundingAdjustment = roundingAdjustment;
    }

    

    

    
}
