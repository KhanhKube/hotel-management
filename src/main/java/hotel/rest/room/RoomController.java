package hotel.rest.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import hotel.service.booking.BookingService;
import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/room")
public class RoomController {

	private final BookingService bookingService;
	private final RoomService roomService;

    @GetMapping
    public String view(Model model) {
        model.addAttribute("listRoom", roomService.getRoomList());
        return "management/room/room-management";
    }

	@PostMapping("/search")
	public ListRoomResponse listRooms(@RequestBody SearchRoomRequest request) {
		return bookingService.listRoom(request);
	}

	@GetMapping("/api/list")
	public ResponseEntity<Map<String, Object>> getRooms() {
		ListRoomResponse rooms = roomService.getAllRoomForSearch();
		Map<String, Object> response = new HashMap<>();
		response.put("data", rooms);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(response);
	}
}
