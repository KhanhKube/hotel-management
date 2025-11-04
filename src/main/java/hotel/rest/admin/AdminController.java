package hotel.rest.admin;

import hotel.db.entity.User;
import hotel.service.account.AccountService;
import hotel.service.hotel.HotelService;
import hotel.service.news.NewsService;
import hotel.service.room.RoomService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HotelService hotelService;
    private final NewsService newsService;
    private final RoomService roomService;
    private final AccountService accountService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        // Get statistics
        long totalRooms = roomService.getAllRooms().size();
        long availableRooms = roomService.getAllRooms().stream()
                .filter(room -> "AVAILABLE".equals(room.getStatus()))
                .count();
        
        // Get total hotels with pagination
        Pageable pageable = PageRequest.of(0, 1);
        long totalHotels = hotelService.getAllHotels(pageable).getTotalElements();
        
        // Get total news
        long totalNews = newsService.findAll().size();
        
        // Get total users
        long totalUsers = accountService.getAllAccounts().size();
        
        // Active bookings - set to 0 for now
        long activeBookings = 0;

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("totalHotels", totalHotels);
        model.addAttribute("totalNews", totalNews);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeBookings", activeBookings);

        return "admin/dashboard";
    }
}
