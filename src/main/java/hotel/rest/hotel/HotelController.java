package hotel.rest.hotel;

// Controller Layer
// HotelController.java

import hotel.db.entity.Hotel;
import hotel.service.hotel.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/management/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    // Display hotel profile (single hotel)
    @GetMapping("/profile")
    public String hotelProfile(Model model) {
        Hotel hotel = hotelService.getDefaultHotel();
        if (hotel == null) {
            // Create default hotel if not exists
            hotel = new Hotel();
            hotel.setName("SeaPalace Hotel");
            hotel.setAddress("123 Beach Road, Coastal City");
            hotel.setPhone("+84 6503 3812");
            hotel.setEmail("info@seapalace.com");
            hotel.setDescription("Luxury beachfront hotel with modern amenities");
            hotel.setStars(5);
            hotel.setStatus("ACTIVE");
            hotel = hotelService.saveHotel(hotel);
        }
        model.addAttribute("hotel", hotel);
        return "management/hotels/profile";
    }

    // Show form to edit hotel profile
    @GetMapping("/edit")
    public String editHotelForm(Model model) {
        Hotel hotel = hotelService.getDefaultHotel();
        if (hotel == null) {
            hotel = new Hotel();
        }
        model.addAttribute("hotel", hotel);
        return "management/hotels/form";
    }

    // Update hotel profile
    @PostMapping("/update")
    public String updateHotel(@ModelAttribute Hotel hotel, Model model) {
        try {
            // Ensure we're updating the default hotel (ID = 1)
            if (hotel.getId() == null) {
                Hotel existingHotel = hotelService.getDefaultHotel();
                if (existingHotel != null) {
                    hotel.setId(existingHotel.getId());
                }
            }
            hotelService.saveHotel(hotel);
            model.addAttribute("success", "Cập nhật thông tin khách sạn thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật: " + e.getMessage());
        }
        return "redirect:/management/hotels/profile";
    }

    // Get hotel info as JSON (for API)
    @GetMapping("/info")
    @ResponseBody
    public Hotel getHotelInfo() {
        return hotelService.getDefaultHotel();
    }
}