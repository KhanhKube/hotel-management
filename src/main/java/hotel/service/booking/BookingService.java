package hotel.service.booking;

import hotel.db.dto.room.ListIdRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;

public interface BookingService {
	ListIdRoomResponse listRoom(SearchRoomRequest request);
}
