package hotel.rest.hotel;

import hotel.db.entity.Hotel;
import hotel.service.hotel.HotelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/hotel")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    // Xem chi tiết khách sạn
    @GetMapping("/{id}")
    public String viewHotel(@PathVariable Long id, Model model) {
        Hotel hotel = hotelService.findById(id);
        model.addAttribute("hotel", hotel);
        return "hotel/view"; // trả về view hotel/view.html
    }

    // Form tạo khách sạn
    @GetMapping("/create")
    public String createHotelForm(Model model) {
        model.addAttribute("hotel", new Hotel());
        return "hotel/form"; // form.html
    }

    // Lưu khách sạn
    @PostMapping("/save")
    public String saveHotel(@ModelAttribute Hotel hotel) {
        hotelService.save(hotel);
        return "redirect:/hotel/" + hotel.getId();
    }

    // Form update
    @GetMapping("/edit/{id}")
    public String editHotelForm(@PathVariable Long id, Model model) {
        Hotel hotel = hotelService.findById(id);
        model.addAttribute("hotel", hotel);
        return "hotel/form";
    }
}


