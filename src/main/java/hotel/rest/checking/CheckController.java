package hotel.rest.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckInResponseDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.dto.checking.CheckOutResponseDto;
import hotel.service.checking.CheckService;
import hotel.util.BaseController;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
@Slf4j
public class CheckController extends BaseController {

    private final CheckService checkService;

    // Constants for template paths and messages
    private static final String CHECKIN_TEMPLATE = "checking/checkin";
    private static final String CHECKOUT_TEMPLATE = "checking/checkout";
    private static final String CHECKIN_LIST_TEMPLATE = "checking/checkin-list";
    private static final String CHECKOUT_LIST_TEMPLATE = "checking/checkout-list";
    private static final String SUCCESS_MESSAGE_KEY = "successMessage";
    private static final String ERROR_MESSAGE_KEY = "errorMessage";

    /**
     * Hiển thị trang check-in
     */
    @GetMapping("/checkin")
    public String showCheckInPage(HttpSession session, Model model) {
        log.info("Showing check-in page");
        
        try {
            // Lấy danh sách phòng trống và khách hàng
            List<Object> availableRooms = checkService.getAvailableRooms();
            List<Object> customers = checkService.getCustomers();
            
            model.addAttribute("availableRooms", availableRooms);
            model.addAttribute("customers", customers);
            model.addAttribute("checkInRequest", new CheckInRequestDto());
            
            return CHECKIN_TEMPLATE;
        } catch (Exception e) {
            log.error("Error showing check-in page", e);
            model.addAttribute(ERROR_MESSAGE_KEY, "Lỗi khi tải trang check-in: " + e.getMessage());
            return "common/error";
        }
    }

    /**
     * Hiển thị trang check-out
     */
    @GetMapping("/checkout")
    public String showCheckOutPage(HttpSession session, Model model) {
        log.info("Showing check-out page");
        
        try {
            // Lấy danh sách bookings có thể check-out
            List<CheckOutResponseDto> checkOutCandidates = checkService.getCheckOutCandidates();
            
            model.addAttribute("checkOutCandidates", checkOutCandidates);
            model.addAttribute("checkOutRequest", new CheckOutRequestDto());
            
            return CHECKOUT_TEMPLATE;
        } catch (Exception e) {
            log.error("Error showing check-out page", e);
            model.addAttribute(ERROR_MESSAGE_KEY, "Lỗi khi tải trang check-out: " + e.getMessage());
            return "common/error";
        }
    }

    /**
     * Hiển thị danh sách check-in
     */
    @GetMapping("/checkin-list")
    public String showCheckInList(HttpSession session, Model model) {
        log.info("Showing check-in list");
        
        try {
            List<CheckInResponseDto> activeCheckIns = checkService.getAllActiveCheckIns();
            model.addAttribute("activeCheckIns", activeCheckIns);
            
            return CHECKIN_LIST_TEMPLATE;
        } catch (Exception e) {
            log.error("Error showing check-in list", e);
            model.addAttribute(ERROR_MESSAGE_KEY, "Lỗi khi tải danh sách check-in: " + e.getMessage());
            return "common/error";
        }
    }

    /**
     * Hiển thị danh sách check-out
     */
    @GetMapping("/checkout-list")
    public String showCheckOutList(HttpSession session, Model model) {
        log.info("Showing check-out list");
        
        try {
            List<CheckOutResponseDto> checkOutHistory = checkService.getCheckInOutHistory();
            model.addAttribute("checkOutHistory", checkOutHistory);
            
            return CHECKOUT_LIST_TEMPLATE;
        } catch (Exception e) {
            log.error("Error showing check-out list", e);
            model.addAttribute(ERROR_MESSAGE_KEY, "Lỗi khi tải danh sách check-out: " + e.getMessage());
            return "common/error";
        }
    }

    /**
     * Thực hiện check-in
     */
    @PostMapping("/checkin/process")
    public String processCheckIn(@Valid @ModelAttribute("checkInRequest") CheckInRequestDto checkInRequestDto,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        log.info("Processing check-in for room: {}", checkInRequestDto.getRoomId());
        
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in check-in request");
            // Lấy lại dữ liệu cần thiết
            List<Object> availableRooms = checkService.getAvailableRooms();
            List<Object> customers = checkService.getCustomers();
            model.addAttribute("availableRooms", availableRooms);
            model.addAttribute("customers", customers);
            return CHECKIN_TEMPLATE;
        }

        try {
            CheckInResponseDto result = checkService.checkIn(checkInRequestDto);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_KEY, 
                "Check-in thành công cho booking #" + result.getBookingId());
            return "redirect:/hotel-management/checkin-list";
        } catch (Exception e) {
            log.error("Error processing check-in", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_KEY, 
                "Lỗi khi thực hiện check-in: " + e.getMessage());
            return "redirect:/hotel-management/checkin";
        }
    }

    /**
     * Thực hiện check-out
     */
    @PostMapping("/checkout/process")
    public String processCheckOut(@Valid @ModelAttribute("checkOutRequest") CheckOutRequestDto checkOutRequestDto,
                                  BindingResult bindingResult,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        log.info("Processing check-out for booking: {}", checkOutRequestDto.getBookingId());
        
        if (bindingResult.hasErrors()) {
            log.error("Validation errors in check-out request");
            // Lấy lại dữ liệu cần thiết
            List<CheckOutResponseDto> checkOutCandidates = checkService.getCheckOutCandidates();
            model.addAttribute("checkOutCandidates", checkOutCandidates);
            return CHECKOUT_TEMPLATE;
        }

        try {
            CheckOutResponseDto result = checkService.checkOut(checkOutRequestDto);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_KEY, 
                "Check-out thành công cho booking #" + result.getBookingId());
            return "redirect:/hotel-management/checkout-list";
        } catch (Exception e) {
            log.error("Error processing check-out", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_KEY, 
                "Lỗi khi thực hiện check-out: " + e.getMessage());
            return "redirect:/hotel-management/checkout";
        }
    }

    /**
     * API endpoint để lấy thông tin booking
     */
    @GetMapping("/api/booking/{bookingId}")
    @ResponseBody
    public CheckInResponseDto getBookingInfo(@PathVariable Integer bookingId) {
        log.info("Getting booking info for ID: {}", bookingId);
        return checkService.getBookingById(bookingId);
    }

    /**
     * API endpoint để kiểm tra phòng có sẵn không
     */
    @GetMapping("/api/room/{roomId}/availability")
    @ResponseBody
    public boolean checkRoomAvailability(@PathVariable Integer roomId,
                                        @RequestParam String checkInDate,
                                        @RequestParam String checkOutDate) {
        log.info("Checking availability for room: {}", roomId);
        return checkService.isRoomAvailable(roomId, checkInDate, checkOutDate);
    }

    /**
     * Xem chi tiết check-in
     */
    @GetMapping("/checkin/{id}")
    public String viewCheckInDetails(@PathVariable Integer id, Model model) {
        log.info("Showing check-in details for booking ID: {}", id);
        try {
            CheckInResponseDto checkIn = checkService.getCheckInById(id);
            if (checkIn == null) {
                log.warn("Check-in not found for ID: {}", id);
                model.addAttribute(ERROR_MESSAGE_KEY, "Không tìm thấy thông tin check-in với ID: " + id);
                return "common/error";
            }
            model.addAttribute("checkIn", checkIn);
            return "checking/checkin-details";
        } catch (Exception e) {
            log.error("Error showing check-in details for ID: {}", id, e);
            model.addAttribute(ERROR_MESSAGE_KEY, "Không thể tải chi tiết check-in: " + e.getMessage());
            return "common/error";
        }
    }
}