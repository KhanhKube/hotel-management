package hotel.db.enums;

public class RoomStatus {
	public static final String AVAILABLE = "AVAILABLE";
	public static final String CHECKIN = "CHECKIN";
	public static final String OCCUPIED = "OCCUPIED";
	public static final String NEED_CLEAN = "NEED_CLEAN";
	public static final String CLEANING = "CLEANING";
	public static final String MAINTENANCE = "MAINTENANCE";
    public static final String EMERGENCYMAINTENANCE = "EMERGENCYMAINTENANCE";

	public static final String[] ALL = {
			AVAILABLE, CHECKIN, OCCUPIED, NEED_CLEAN, CLEANING, MAINTENANCE, EMERGENCYMAINTENANCE
	};
}
