package hotel.rest.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import hotel.db.entity.Room;
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

    /*
    View danh sách các phòng
    */
    @GetMapping
    public String view(Model model) {
        model.addAttribute("listRoom", roomService.getRoomList());
        return "management/room/room-management";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("statuses", roomService.getAllRooms());
        model.addAttribute("roomTypes", roomService.getAllRoomTypes());
        model.addAttribute("bedTypes", roomService.getAllBedTypes());
        model.addAttribute("room", new Room());
        return "management/room/room-create-form";
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
