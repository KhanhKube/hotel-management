package hotel.rest.checking;

import hotel.db.dto.checking.OrderDetailResponse;
import hotel.db.entity.User;
import hotel.service.checking.CheckingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CheckingViewController {

    private final CheckingService checkingService;

    @GetMapping("/hotel-management/checking")
    public String checkingPageDefault() {
        return "redirect:/hotel-management/checking/page?tab=checkin";
    }
    
    @GetMapping("/api/checking/page")
    public String apiCheckingPage(@RequestParam(defaultValue = "checkin") String tab, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size, HttpSession session, Model model) {
        return checkingPage(tab, page, size, session, model);
    }

    @GetMapping("/hotel-management/checking/page")
    public String checkingPage(@RequestParam(defaultValue = "checkin") String tab, 
                              @RequestParam(defaultValue = "0") int page, 
                              @RequestParam(defaultValue = "12") int size, 
                              HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("userRole", user != null ? user.getRole() : "GUEST");
        
        Pageable pageable = PageRequest.of(page, size);
        Pageable countPageable = PageRequest.of(0, 1);
        
        // Tính số lượng cho từng tab
        long checkinCount = checkingService.getReservedOrders(countPageable).getTotalElements() 
                          + checkingService.getCheckingInOrders(countPageable).getTotalElements() 
                          + checkingService.getCustomerConfirmedOrders(countPageable).getTotalElements();
        
        long checkoutCount = checkingService.getOccupiedOrders(countPageable).getTotalElements() 
                           + checkingService.getNeedCheckOutOrders(countPageable).getTotalElements() 
                           + checkingService.getCheckingOutOrders(countPageable).getTotalElements() 
                           + checkingService.getCheckedOutOrders(countPageable).getTotalElements();
        
        long afterCheckoutCount = checkingService.getNeedCleanOrders(countPageable).getTotalElements() 
                                + checkingService.getCleaningOrders(countPageable).getTotalElements();
        
        // Lấy dữ liệu theo tab
        Page<OrderDetailResponse> roomsPage;
        if ("checkout".equals(tab)) {
            roomsPage = getCheckoutRooms(pageable);
        } else if ("aftercheckout".equals(tab)) {
            roomsPage = getAfterCheckoutRooms(pageable);
        } else {
            roomsPage = getCheckinRooms(pageable);
            tab = "checkin";
        }
        
        model.addAttribute("currentTab", tab);
        model.addAttribute("checkinCount", checkinCount);
        model.addAttribute("checkoutCount", checkoutCount);
        model.addAttribute("afterCheckoutCount", afterCheckoutCount);
        model.addAttribute("rooms", roomsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomsPage.getTotalPages());
        model.addAttribute("totalItems", roomsPage.getTotalElements());
        model.addAttribute("startItem", page * size + 1);
        model.addAttribute("endItem", Math.min((page + 1) * size, (int) roomsPage.getTotalElements()));
        
        return "checking/checking-new";
    }
    
    private Page<OrderDetailResponse> getCheckinRooms(Pageable pageable) {
        Pageable fetchAll = PageRequest.of(0, 100);
        List<OrderDetailResponse> rooms = new ArrayList<>();
        rooms.addAll(checkingService.getReservedOrders(fetchAll).getContent());
        rooms.addAll(checkingService.getCheckingInOrders(fetchAll).getContent());
        rooms.addAll(checkingService.getCustomerConfirmedOrders(fetchAll).getContent());
        return new PageImpl<>(rooms, pageable, rooms.size());
    }
    
    private Page<OrderDetailResponse> getCheckoutRooms(Pageable pageable) {
        Pageable fetchAll = PageRequest.of(0, 100);
        List<OrderDetailResponse> rooms = new ArrayList<>();
        rooms.addAll(checkingService.getOccupiedOrders(fetchAll).getContent());
        rooms.addAll(checkingService.getNeedCheckOutOrders(fetchAll).getContent());
        rooms.addAll(checkingService.getCheckingOutOrders(fetchAll).getContent());
        rooms.addAll(checkingService.getCheckedOutOrders(fetchAll).getContent());
        return new PageImpl<>(rooms, pageable, rooms.size());
    }
    
    private Page<OrderDetailResponse> getAfterCheckoutRooms(Pageable pageable) {
        List<OrderDetailResponse> rooms = new ArrayList<>();
        rooms.addAll(checkingService.getNeedCleanOrders(pageable).getContent());
        rooms.addAll(checkingService.getCleaningOrders(pageable).getContent());
        return new PageImpl<>(rooms, pageable, rooms.size());
    }
}

