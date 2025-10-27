package hotel.rest.order;

import hotel.db.dto.order.BookingInfoDto;
import hotel.db.dto.order.OrderDto;
import hotel.service.order.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/hotel")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/order")
    public String viewOrders(Model model, HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/hotel/login";
        }

        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    /**
     * API endpoint: Lấy thông tin đặt phòng của user
     * Trả về: orderId, orderDetailId, checkIn (ngày đặt), checkOut (ngày hết hạn), roomNumber, roomType, status
     */
    @GetMapping("/api/booking-info")
    @ResponseBody
    public ResponseEntity<List<BookingInfoDto>> getBookingInfo(HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        if (userId == null) {
            return ResponseEntity.ok(List.of());
        }

        List<BookingInfoDto> bookingInfo = orderService.getBookingInfoByUserId(userId);
        return ResponseEntity.ok(bookingInfo);
    }

    /**
     * API endpoint: Lấy tất cả thông tin đặt phòng (cho admin)
     * Trả về: orderId, orderDetailId, checkIn (ngày đặt), checkOut (ngày hết hạn), roomNumber, roomType, status
     */
    @GetMapping("/api/all-booking-info")
    @ResponseBody
    public ResponseEntity<List<BookingInfoDto>> getAllBookingInfo() {
        List<BookingInfoDto> bookingInfo = orderService.getAllBookingInfo();
        return ResponseEntity.ok(bookingInfo);
    }

    private Integer getUserIdFromSession(HttpSession session) {
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj != null) {
            return (Integer) userIdObj;
        }

        Object user = session.getAttribute("user");
        if (user != null) {
            try {
                return (Integer) user.getClass().getMethod("getUserId").invoke(user);
            } catch (Exception e) {
                // Ignore
            }
        }

        return 1; // Temporary for testing
    }
}
