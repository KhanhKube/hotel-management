package hotel.rest.checkincheckout;

import hotel.db.entity.Order;
import hotel.db.entity.Room;
import hotel.db.entity.User;
import hotel.service.checkincheckout.CheckInCheckOutService;
import hotel.util.BaseController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/hotel-management")
@RequiredArgsConstructor
@Slf4j
public class CheckInCheckOutController extends BaseController {

    private final CheckInCheckOutService checkInCheckOutService;

    /**
     * Trang chính check-in/check-out
     */
    @GetMapping("/checkin-checkout")
    public String checkInCheckOutMain(Model model) {
        List<Order> activeOrders = checkInCheckOutService.getActiveOrders();
        model.addAttribute("activeOrders", activeOrders);
        return "checkincheckout/main";
    }

    /**
     * Form check-in
     */
    @GetMapping("/checkin")
    public String checkInForm(Model model) {
        List<Room> availableRooms = checkInCheckOutService.getAvailableRooms();
        List<User> customers = checkInCheckOutService.getCustomers();
        
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("customers", customers);
        return "checkincheckout/checkin";
    }

    /**
     * Xử lý check-in
     */
    @PostMapping("/checkin")
    public String processCheckIn(@RequestParam Integer roomId,
                                 @RequestParam Integer customerId,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            Order result = checkInCheckOutService.processCheckIn(roomId, customerId, notes);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Check-in thành công! Order ID: " + result.getOrderId());
            return "redirect:/hotel-management/checkin-checkout?success=checkin";
        } catch (Exception e) {
            log.error("Error processing check-in: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/hotel-management/checkin";
        }
    }

    /**
     * Form check-out
     */
    @GetMapping("/checkout/{orderId}")
    public String checkOutForm(@PathVariable Integer orderId, Model model) {
        try {
            Order order = checkInCheckOutService.getOrderById(orderId);
            Room room = checkInCheckOutService.getRoomFromOrder(order);
            User customer = checkInCheckOutService.getCustomerFromOrder(order);
            
            model.addAttribute("order", order);
            model.addAttribute("room", room);
            model.addAttribute("customer", customer);
            return "checkincheckout/checkout";
        } catch (Exception e) {
            log.error("Error loading check-out form: {}", e.getMessage());
            return "redirect:/hotel-management/checkin-checkout?error=load";
        }
    }

    /**
     * Xử lý check-out
     */
    @PostMapping("/checkout")
    public String processCheckOut(@RequestParam Integer orderId,
                                 @RequestParam(required = false) String notes,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            Order result = checkInCheckOutService.processCheckOut(orderId, notes);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Check-out thành công! Order ID: " + result.getOrderId());
            return "redirect:/hotel-management/checkin-checkout?success=checkout";
        } catch (Exception e) {
            log.error("Error processing check-out: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/hotel-management/checkout/" + orderId;
        }
    }

    /**
     * Chi tiết Order
     */
    @GetMapping("/order-details/{orderId}")
    public String orderDetails(@PathVariable Integer orderId, Model model) {
        try {
            Order order = checkInCheckOutService.getOrderById(orderId);
            Room room = checkInCheckOutService.getRoomFromOrder(order);
            User customer = checkInCheckOutService.getCustomerFromOrder(order);
            
            model.addAttribute("order", order);
            model.addAttribute("room", room);
            model.addAttribute("customer", customer);
            return "checkincheckout/order-details";
        } catch (Exception e) {
            log.error("Error getting order details: {}", e.getMessage());
            return "redirect:/hotel-management/checkin-checkout?error=details";
        }
    }
}

