package hotel.rest.checking;

import hotel.db.dto.checking.AfterCheckOutRequestDto;
import hotel.db.dto.checking.BookingDto;
import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.entity.User;
import hotel.service.checking.CheckingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller xử lý check-in, check-out và after check-out
 * Đơn giản, mượt mà, load nhanh
 */
@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
public class CheckingController {

    private final CheckingService checkingService;

    /**
     * Trang quản lý check-in, check-out và after check-out
     * Một trang duy nhất với 3 tabs
     */
    @GetMapping("/checking")
    public String checkingManagement(
            @RequestParam(value = "type", defaultValue = "checkin") String type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpSession session,
            Model model) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        model.addAttribute("currentType", type);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // Đếm số lượng cho badges (nhanh)
        model.addAttribute("checkInCount", checkingService.countCheckInItems());
        model.addAttribute("checkOutCount", checkingService.countCheckOutItems());
        model.addAttribute("afterCheckOutCount", checkingService.countAfterCheckOutItems());

        // Load dữ liệu với pagination (chỉ load tab hiện tại)
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingDto> bookingPage;

        switch (type) {
            case "checkin":
                bookingPage = checkingService.getCheckInList(pageable);
                model.addAttribute("bookings", bookingPage.getContent());
                break;
            case "checkout":
                bookingPage = checkingService.getCheckOutList(pageable);
                model.addAttribute("bookings", bookingPage.getContent());
                break;
            case "after-checkout":
                bookingPage = checkingService.getAfterCheckOutList(pageable);
                model.addAttribute("bookings", bookingPage.getContent());
                break;
            default:
                bookingPage = checkingService.getCheckInList(pageable);
                model.addAttribute("bookings", bookingPage.getContent());
        }

        model.addAttribute("totalItems", bookingPage.getTotalElements());
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("isFirst", bookingPage.isFirst());
        model.addAttribute("isLast", bookingPage.isLast());
        
        // Tính toán hiển thị phân trang
        long totalElements = bookingPage.getTotalElements();
        int currentPageNum = bookingPage.getNumber();
        int pageSizeNum = bookingPage.getSize();
        long startItem = totalElements > 0 ? ((long) currentPageNum * pageSizeNum + 1) : 0;
        long endItem = totalElements > 0 ? Math.min((long) (currentPageNum + 1) * pageSizeNum, totalElements) : 0;
        model.addAttribute("startItem", startItem);
        model.addAttribute("endItem", endItem);

        return "checking/checking";
    }

    /**
     * Xử lý check-in
     */
    @PostMapping("/checking/checkin")
    public String checkIn(
            @ModelAttribute CheckInRequestDto request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            checkingService.checkIn(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Check-in thành công cho booking #" + request.getOrderDetailId());
            return "redirect:/hotel-management/checking?type=checkout&page=0&size=10";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checking?type=checkin&page=0&size=10";
        }
    }

    /**
     * Xử lý check-out
     */
    @PostMapping("/checking/checkout")
    public String checkOut(
            @ModelAttribute CheckOutRequestDto request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            checkingService.checkOut(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Check-out thành công cho booking #" + request.getOrderDetailId());
            return "redirect:/hotel-management/checking?type=after-checkout&page=0&size=10";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checking?type=checkout&page=0&size=10";
        }
    }

    /**
     * Xử lý after check-out
     */
    @PostMapping("/checking/after-checkout")
    public String afterCheckOut(
            @ModelAttribute AfterCheckOutRequestDto request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/hotel/login";
        }

        try {
            checkingService.afterCheckOut(request);
            String message = request.getReadyForNextGuest() 
                    ? "Phòng đã sẵn sàng cho khách mới" 
                    : "Phòng đã chuyển sang bảo trì";
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Xử lý thành công. " + message);
            return "redirect:/hotel-management/checking?type=after-checkout&page=0&size=10";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hotel-management/checking?type=after-checkout&page=0&size=10";
        }
    }
}

