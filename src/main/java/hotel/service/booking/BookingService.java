package hotel.service.booking;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;

public interface BookingService {
	ListRoomResponse listRoom(SearchRoomRequest request);
}
