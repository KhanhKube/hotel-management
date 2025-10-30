package hotel.rest.homepage;

import hotel.db.dto.room.RoomBookListDto;
import hotel.db.dto.room.RoomDetailResponseDto;
import hotel.db.dto.room.RoomHomepageResponseDto;
import hotel.db.entity.News;
import hotel.db.enums.BedType;
import hotel.db.enums.RoomType;
import hotel.db.repository.floor.FloorRepository;
import hotel.service.room.RoomService;
import hotel.service.news.NewsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

import static hotel.db.enums.Constants.LOGININVALID;
import static hotel.db.enums.Constants.ROOMNOTEXIST;

@Controller
@RequestMapping("/hotel")
@RequiredArgsConstructor
public class HomePageController {

    private final RoomService roomService;
    private final NewsService newsService;
    private final FloorRepository floorRepository;

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


    @ModelAttribute
    public void loadDropdownData(Model model) {
        model.addAttribute("roomTypes", RoomType.ALL);
        model.addAttribute("bedTypes", BedType.ALL);
        model.addAttribute("floors", roomService.getAllFloors());
        model.addAttribute("sizes", roomService.getAllSizes());
    }

    @GetMapping("/rooms")
    public String viewListRooms(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String bedType,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Page<RoomBookListDto> roomPage = roomService.getRoomListWithFiltersAndPagination(
                minPrice, maxPrice, roomType, floor, bedType, sortBy, page, size
        );
        //field pagnitation
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("totalItems", roomPage.getTotalElements());
        //field thanh filter
        model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("selectedFloor", floor);
        model.addAttribute("selectedBedType", bedType);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("selectedMinPrice", minPrice);
        model.addAttribute("selectedMaxPrice", maxPrice);
        return "common/room-booklist";
    }

}
