package com.buffetrestaurant.exception;

/** Stable domain reasons; keep ErrorResponse's existing JSON shape. */
public class MenuConflictException extends RuntimeException {
    public enum Reason {
        CATEGORY_HAS_ITEMS("หมวดหมู่นี้ยังมีเมนูในรายการใช้งาน กรุณาย้ายหรือเก็บเมนูออกก่อน"),
        CATEGORY_ARCHIVED("หมวดหมู่นี้ถูกเก็บออกแล้ว กรุณาคืนหมวดหมู่ก่อนคืนหรือเพิ่มเมนู"),
        ITEM_ARCHIVED("เมนูนี้ถูกเก็บออกแล้ว ไม่สามารถสั่งหรือแก้ไขได้ กรุณาโหลดรายการใหม่"),
        CATEGORY_CHANGED("หมวดหมู่ของเมนูเปลี่ยนระหว่างทำรายการ กรุณาโหลดรายการใหม่");

        private final String message;
        Reason(String message) { this.message = message; }
    }

    private final Reason reason;
    public MenuConflictException(Reason reason) { super(reason.message); this.reason = reason; }
    public Reason getReason() { return reason; }
}
