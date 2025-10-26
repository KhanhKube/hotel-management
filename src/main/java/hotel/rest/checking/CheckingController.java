package hotel.rest.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckInResponseDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.entity.Room;
import hotel.db.entity.User;
import hotel.service.checking.CheckingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class CheckingController {

    private final CheckingService checkingService;

    // ===== CHECK-IN ROUTES =====

    @GetMapping("/checkin-list")
    public String checkInList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        List<CheckInResponseDto> activeCheckIns = checkingService.getAllActiveCheckIns();
        model.addAttribute("activeCheckIns", activeCheckIns);
        return "checking/checkin-list";
    }
    
    @GetMapping("/checkout-list")
    public String checkOutList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        List<CheckInResponseDto> checkOutHistory = checkingService.getAllCheckOutHistory();
        model.addAttribute("checkOutHistory", checkOutHistory);
        return "checking/checkout-list";
    }

    @GetMapping("/checkin")
    public String checkInForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        // Get available rooms (status = "Trống")
        List<Room> availableRooms = checkingService.getAvailableRooms();

        // Get all customers
        List<User> customers = checkingService.getAllCustomers();

        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("customers", customers);
        model.addAttribute("checkInRequest", CheckInRequestDto.builder().build());
        return "checking/checkin";
    }

    @PostMapping("/checkin/process")
    public String processCheckIn(@ModelAttribute CheckInRequestDto request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            CheckInResponseDto response = checkingService.processCheckIn(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Check-in thành công cho booking #" + response.getBookingId());
            return "redirect:/hotel-management/checkin-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checkin";
        }
    }

    @GetMapping("/checkin/{id}")
    public String checkInDetails(@PathVariable Integer id, 
                                 HttpSession session, 
                                 Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            CheckInResponseDto checkIn = checkingService.getCheckInById(id);
            model.addAttribute("checkIn", checkIn);
            return "checking/checkin-details";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checkin-list";
        }
    }

    // ===== CHECK-OUT ROUTES =====

    @GetMapping("/checkout")
    public String checkOutForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        // Get bookings that are checked-in and ready for check-out
        List<CheckInResponseDto> checkOutCandidates = checkingService.getCheckOutCandidates();

        model.addAttribute("checkOutCandidates", checkOutCandidates);
        model.addAttribute("checkOutRequest", CheckOutRequestDto.builder().build());
        return "checking/checkout";
    }

    @PostMapping("/checkout/process")
    public String processCheckOut(@ModelAttribute CheckOutRequestDto request,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            checkingService.processCheckOut(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Check-out thành công cho booking #" + request.getBookingId());
            return "redirect:/hotel-management/checkout/complete/" + request.getBookingId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checkout";
        }
    }

    @GetMapping("/checkout/complete/{id}")
    public String checkOutComplete(@PathVariable Integer id, 
                                   HttpSession session, 
                                   Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            CheckInResponseDto completedCheckOut = checkingService.getCheckOutById(id);
            model.addAttribute("completedCheckOut", completedCheckOut);
            return "checking/checkout-complete";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checkout-list";
        }
    }
}

