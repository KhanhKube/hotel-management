package hotel.rest.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import hotel.service.booking.BookingService;
import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
//    private final RoomRepository roomRepo;
//
//    @GetMapping
//    public List<Room> list(@RequestParam(required = false) RoomStatus status) {
//        return status==null ? roomRepo.findAll() : roomRepo.findByStatus(status);
//    }
//
//    @PatchMapping("/{id}/status")
//    public Room changeStatus(@PathVariable Long id, @RequestParam RoomStatus status) {
//        Room r = roomRepo.findById(id).orElseThrow();
//        r.setStatus(status);
//        return roomRepo.save(r);
//    }

	private final BookingService bookingService;
	private final RoomService roomService;

	@PostMapping("/search")
	public ListRoomResponse listRooms(@RequestBody SearchRoomRequest request) {
		return bookingService.listRoom(request);
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> getRooms() {
		ListRoomResponse rooms = roomService.getAllRoomForSearch();
		Map<String, Object> response = new HashMap<>();
		response.put("data", rooms);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(response);
	}
}
