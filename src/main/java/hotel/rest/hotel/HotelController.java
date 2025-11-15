package hotel.rest.hotel;

// Controller Layer
// HotelController.java

import hotel.db.entity.Hotel;
import hotel.db.entity.User;
import hotel.service.hotel.HotelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static hotel.db.enums.Constants.*;

@Controller
@RequestMapping("/management/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    // List all hotels with paging, search, and filters
    @GetMapping
    public String listHotels(@RequestParam(required = false) String name,
                             @RequestParam(required = false) Integer stars,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(defaultValue = "id") String sortBy,
                             @RequestParam(defaultValue = "asc") String sortDir,
                             HttpSession session,
                             Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (!user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Hotel> hotels;
        if (name != null && !name.isEmpty()) {
            hotels = hotelService.searchHotelsByName(name, pageable);
        } else if (stars != null) {
            hotels = hotelService.filterHotelsByStars(stars, pageable);
        } else if (status != null && !status.isEmpty()) {
            hotels = hotelService.filterHotelsByStatus(status, pageable);
        } else {
            hotels = hotelService.getHotelsWithFilters(name, stars, status, pageable);
        }

        model.addAttribute("hotels", hotels);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", hotels.getTotalPages());
        model.addAttribute("name", name);
        model.addAttribute("stars", stars);
        model.addAttribute("status", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        return "management/hotels/list";
    }

    // Show form for new hotel
    @GetMapping("/new")
    public String newHotelForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (!user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }
        model.addAttribute("hotel", new Hotel());
        return "management/hotels/form";
    }

    // Show form for edit hotel
    @GetMapping("/edit/{id}")
    public String editHotelForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (!user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }
        Optional<Hotel> hotelOpt = hotelService.getHotelById(id);
        if (hotelOpt.isPresent()) {
            model.addAttribute("hotel", hotelOpt.get());
            return "management/hotels/form";
        } else {
            return "redirect:/management/hotels";
        }
    }

    // Save hotel (create or update)
    @PostMapping
    public String saveHotel(@ModelAttribute Hotel hotel, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (!user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }
        hotelService.saveHotel(hotel);
        return "redirect:/management/hotels";
    }

    // Delete hotel
    @GetMapping("/delete/{id}")
    public String deleteHotel(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (!user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }
        hotelService.deleteHotel(id);
        return "redirect:/management/hotels";
    }

    // View hotel details
    @GetMapping("/view/{id}")
    public String viewHotel(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel";
        }
        if (user.getRole().equals(CUSTOMER)) {
            return "redirect:/hotel";
        }
        if (!user.getRole().equals(ADMIN)) {
            return "redirect:/hotel/dashboard";
        }
        Optional<Hotel> hotelOpt = hotelService.getHotelById(id);
        if (hotelOpt.isPresent()) {
            Hotel hotel = hotelOpt.get();
            // Increment view count
            //  hotel.setViewCount(hotel.getViewCount() + 1);
            //   hotelService.saveHotel(hotel);
            model.addAttribute("hotel", hotel);
            return "management/hotels/view";
        } else {
            return "redirect:/management/hotels";
        }
    }
}