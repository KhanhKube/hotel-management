package hotel.rest.homepage;

import hotel.db.dto.room.RoomHomepageResponseDto;
import hotel.service.room.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HomePageController {

    private final RoomService roomService;

    @GetMapping({"/", "", "/home"})
    public String home(HttpSession session, Model model) {
        List<RoomHomepageResponseDto> rooms = roomService.getTop3Rooms();
        model.addAttribute("rooms", rooms);

        return "common/home";
    }


}
