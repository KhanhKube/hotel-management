package hotel.db.enums;

public class RoomSystemStatus {
    public static final String WORKING = "WORKING";
    public static final String MAINTENANCE = "MAINTENANCE";
    public static final String NEARMAINTENANCE = "NEARMAINTENANCE";
    public static final String  STOPWORKING = "STOPWORKING";
    public static final String NEARSTOPWORKING = "NEARSTOPWORKING";

    public static final String[] ALL = {
            WORKING, MAINTENANCE, STOPWORKING
    };
}
