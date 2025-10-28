package hotel.rest.homepage;

import hotel.db.dto.room.RoomDetailResponseDto;
import hotel.db.dto.room.RoomHomepageResponseDto;
import hotel.db.entity.News;
import hotel.service.room.RoomService;
import hotel.service.news.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static hotel.db.enums.Constants.LOGININVALID;
import static hotel.db.enums.Constants.ROOMNOTEXIST;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HomePageController {

    private final RoomService roomService;
    private final NewsService newsService;

    @GetMapping({"/", "", "/home"})
    public String home(HttpSession session, Model model) {
        List<RoomHomepageResponseDto> rooms = roomService.getTop3Rooms();
        model.addAttribute("rooms", rooms);

        // Get latest 3 published news for home page
        List<News> allPublishedNews = newsService.findByStatus("PUBLISHED");
        List<News> latestNews = allPublishedNews.stream()
                .filter(news -> news.getCreatedAt() != null) // Null safety
                .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
                .limit(3)
                .toList();
        model.addAttribute("latestNews", latestNews);

        return "common/home";
    }

    @GetMapping({"/room/{id}"})
    public String roomDetail(@PathVariable("id") Integer id,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttrs) {
        RoomDetailResponseDto room = roomService.getRoomDetailById(id);

        roomService.incrementView(id);

        if(room == null) {
            redirectAttrs.addFlashAttribute("error", ROOMNOTEXIST);
            return "redirect:/hotel";
        }
        model.addAttribute("room", room);
        return "common/room-detail";
    }

}
