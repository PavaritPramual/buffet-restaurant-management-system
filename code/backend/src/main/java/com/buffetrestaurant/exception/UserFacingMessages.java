package com.buffetrestaurant.exception;

public final class UserFacingMessages {
    public static final String TABLE_NOT_AVAILABLE = "โต๊ะไม่ว่าง กรุณาเลือกโต๊ะที่พร้อมใช้งาน";
    public static final String TABLE_OCCUPIED_CANNOT_DELETE = "ไม่สามารถลบโต๊ะได้ เนื่องจากโต๊ะยังไม่ว่าง";
    public static final String TABLE_HAS_ACTIVE_SESSION = "ไม่สามารถแก้ไขหรือลบโต๊ะได้ ขณะยังมีรอบใช้งานอยู่";
    public static final String TABLE_HAS_DINING_HISTORY = "โต๊ะนี้มีประวัติการใช้งาน จึงลบถาวรไม่ได้";
    public static final String MENU_ITEM_HAS_ORDER_HISTORY =
            "เมนูนี้มีประวัติการสั่งซื้อ จึงลบถาวรไม่ได้";
    public static final String CATEGORY_HAS_ITEMS =
            "หมวดหมู่นี้ยังมีเมนูอยู่ กรุณาย้ายเมนูไปหมวดอื่นก่อนลบหมวดหมู่นี้";

    private UserFacingMessages() {}

    public static String guestCountExceedsCapacity(int capacity) {
        return "จำนวนผู้ใช้บริการเกินความจุของโต๊ะ (สูงสุด %d คน)".formatted(capacity);
    }
}
