package hotel.rest.room;

import hotel.service.room.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomViewController {
    private final RoomService roomService;

    @GetMapping
    public String viewListRooms (Model model) {
        model.addAttribute("rooms", roomService.getRoomListForBooking());
        return "common/room-booklist";
    }
}
