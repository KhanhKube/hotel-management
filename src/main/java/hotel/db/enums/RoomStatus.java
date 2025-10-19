package hotel.db.enums;

public class RoomStatus {
    public static final String AVAILABLE = "Trống";
    public static final String OCCUPIED = "Đang thuê";
    public static final String MAINTENANCE = "Bảo trì";
    public static final String RESERVED = "Đã đặt";
    
    public static final String[] ALL = {
        AVAILABLE, OCCUPIED, MAINTENANCE, RESERVED
    };
}
