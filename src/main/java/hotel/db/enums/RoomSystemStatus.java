package hotel.db.enums;

public class RoomSystemStatus {
    public static final String WORKING = "Hoạt động";
    public static final String MAINTENANCE = "Bảo trì";
    public static final String  STOPWORKING = "Dừng hoạt động";

    public static final String[] ALL = {
            WORKING, MAINTENANCE, STOPWORKING
    };
}
