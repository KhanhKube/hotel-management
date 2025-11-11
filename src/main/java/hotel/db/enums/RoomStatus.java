package hotel.db.enums;

public class RoomStatus {
    public static final String AVAILABLE = "Đang trống";
    public static final String CHECKIN = "Đang check-in";
    public static final String OCCUPIED = "Đang thuê";
    public static final String NEED_CLEAN = "Cần dọn dẹp";
    public static final String CLEANING = "Đang dọn dẹp";
    public static final String MAINTENANCE = "Đang bảo trì";
    
    public static final String[] ALL = {
        AVAILABLE, CHECKIN, OCCUPIED, NEED_CLEAN, CLEANING, MAINTENANCE
    };
}
