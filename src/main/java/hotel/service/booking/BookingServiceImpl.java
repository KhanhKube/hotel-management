package hotel.service.booking;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {
	private final SearchRoomService searchRoomService;

	@Override
	public ListRoomResponse listRoom(SearchRoomRequest request) {
		ListRoomResponse response = searchRoomService.getRoom(request);

		// Có thể log hoặc kiểm tra ở đây
		if (response == null || response.getData() == null || response.getData().isEmpty()) {
			throw new RuntimeException("response is null or empty");
		}

		return response;
	}

}
