package hotel.rest.room;

import hotel.db.dto.room.ListRoomResponse;
import hotel.db.dto.room.SearchRoomRequest;
import hotel.db.entity.Discount;
import hotel.db.entity.Room;
import hotel.service.booking.BookingService;
import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel-management/room")
public class RoomController {

	private final BookingService bookingService;
	private final RoomService roomService;
	private final hotel.db.repository.floor.FloorRepository floorRepository;
	private final hotel.db.repository.size.SizeRepository sizeRepository;

    // Tự động load data cho dropdown trong mọi request
    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("roomTypes", roomService.getAllRoomTypes());
        model.addAttribute("bedTypes", roomService.getAllBedTypes());
        model.addAttribute("statuses", roomService.getAllStatus());
        model.addAttribute("floors", floorRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("listRoom", roomService.getRoomList());
        return "management/room/room-management";
    }

    @GetMapping("/create")
    public String create(Model model) {
        Room room = new Room();
        model.addAttribute("room", room);
        return "management/room/room-create-form";
    }
    @PostMapping("/create")
    public String create(@ModelAttribute("room") Room room,
                         BindingResult result, Model model) {
        if (room.getRoomId() == null) {
            if (roomService.checkForCreateRoomNumber(room.getRoomNumber())) {
                model.addAttribute("room", room);
                model.addAttribute("errorMessage", "Số phòng này đã tồn tại, vui lòng lấy số phòng khác!");
                return "management/room/room-create-form";
            }
        } else {
            if (!roomService.checkForEditRoomNumber(room.getRoomNumber(), Long.valueOf(room.getRoomId()))) {
                model.addAttribute("room", room);
                model.addAttribute("errorMessage", "Số phòng này đã tồn tại, vui lòng lấy số phòng khác!");
                return "management/room/room-create-form";
            }
        }
        HashMap<String, String> saveResult = roomService.saveRoom(room);

        if (saveResult.containsKey("error")) {
            model.addAttribute("room", room);
            model.addAttribute("errorMessage", saveResult.get("error"));
            return "management/room/room-create-form";
        }
        return "redirect:/hotel-management/room";
    }

    @GetMapping("/edit/{id}")
    public String editRoomtForm(@PathVariable Integer id, Model model) {
        Room room =  roomService.getRoomById(id);
        if  (room == null) {
            return "redirect:/hotel-management/room";
        }
        model.addAttribute("room", room);
        return "management/room/room-create-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Integer id, Model model) {
        // Lấy room để check status
        Room room = roomService.getRoomById(id);
        
        if (room == null) {
            model.addAttribute("errorMessage", "Không tìm thấy phòng!");
            model.addAttribute("listRoom", roomService.getRoomList());
            return "management/room/room-management";
        }
        
        // Không cho xóa nếu phòng đang hoạt động (Đang thuê hoặc Đã đặt)
        if ("Đang thuê".equals(room.getStatus()) || "Đã đặt".equals(room.getStatus())) {
            model.addAttribute("errorMessage", "Không thể xóa phòng đang hoạt động! Phòng đang ở trạng thái: " + room.getStatus());
            model.addAttribute("listRoom", roomService.getRoomList());
            return "management/room/room-management";
        }
        
        // Cho phép xóa nếu Trống hoặc Bảo trì
        roomService.DeleteRoom(id);
        return "redirect:/hotel-management/room";
    }

    @GetMapping("/detail/{id}")
    public String detailRoom(@PathVariable Integer id,Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        return "management/room/room-detail";
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
