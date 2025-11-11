package hotel.db.enums;

public class RoomStatus {
    public static final String AVAILABLE = "Đang trống";
    public static final String OCCUPIED = "Đang thuê";
    public static final String CLEANING = "Đang dọn dẹp";
    public static final String RESERVED = "Đã được đặt";
    public static final String MAINTENANCE = "Phòng cần bảo trì";
    public static final String[] ALL = {
        AVAILABLE, OCCUPIED, CLEANING
    };
}
