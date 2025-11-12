package hotel.rest.checking;

import hotel.db.dto.checking.OrderDetailResponse;
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
    public String checkingPage(@RequestParam(defaultValue = "checkin") String tab, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size, HttpSession session, Model model) {
        hotel.db.entity.User user = (hotel.db.entity.User) session.getAttribute("user");
        String userRole = user != null ? user.getRole() : "GUEST";
        model.addAttribute("userRole", userRole);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDetailResponse> roomsPage;
        long checkinCount = checkingService.getReservedOrders(PageRequest.of(0, 1)).getTotalElements() + checkingService.getCheckingInOrders(PageRequest.of(0, 1)).getTotalElements() + checkingService.getCustomerConfirmedOrders(PageRequest.of(0, 1)).getTotalElements();
        long checkoutCount = checkingService.getOccupiedOrders(PageRequest.of(0, 1)).getTotalElements() + checkingService.getNeedCheckOutOrders(PageRequest.of(0, 1)).getTotalElements() + checkingService.getCheckingOutOrders(PageRequest.of(0, 1)).getTotalElements() + checkingService.getCheckedOutOrders(PageRequest.of(0, 1)).getTotalElements();
        long afterCheckoutCount = checkingService.getNeedCleanOrders(PageRequest.of(0, 1)).getTotalElements() + checkingService.getCleaningOrders(PageRequest.of(0, 1)).getTotalElements();
        if ("checkin".equals(tab)) {
            Page<OrderDetailResponse> reservedPage = checkingService.getReservedOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> checkingInPage = checkingService.getCheckingInOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> customerConfirmPage = checkingService.getCustomerConfirmedOrders(PageRequest.of(0, 100));
            List<OrderDetailResponse> allCheckinRooms = new ArrayList<>();
            allCheckinRooms.addAll(reservedPage.getContent());
            allCheckinRooms.addAll(checkingInPage.getContent());
            allCheckinRooms.addAll(customerConfirmPage.getContent());
            roomsPage = new PageImpl<>(allCheckinRooms, pageable, reservedPage.getTotalElements() + checkingInPage.getTotalElements() + customerConfirmPage.getTotalElements());
        } else if ("checkout".equals(tab)) {
            Page<OrderDetailResponse> occupiedPage = checkingService.getOccupiedOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> needCheckoutPage = checkingService.getNeedCheckOutOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> checkingOutPage = checkingService.getCheckingOutOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> checkedOutPage = checkingService.getCheckedOutOrders(PageRequest.of(0, 100));
            List<OrderDetailResponse> allCheckoutRooms = new ArrayList<>();
            allCheckoutRooms.addAll(occupiedPage.getContent());
            allCheckoutRooms.addAll(needCheckoutPage.getContent());
            allCheckoutRooms.addAll(checkingOutPage.getContent());
            allCheckoutRooms.addAll(checkedOutPage.getContent());
            roomsPage = new PageImpl<>(allCheckoutRooms, pageable, occupiedPage.getTotalElements() + needCheckoutPage.getTotalElements() + checkingOutPage.getTotalElements() + checkedOutPage.getTotalElements());
        } else if ("aftercheckout".equals(tab)) {
            Page<OrderDetailResponse> needCleanPage = checkingService.getNeedCleanOrders(pageable);
            Page<OrderDetailResponse> cleaningPage = checkingService.getCleaningOrders(pageable);
            List<OrderDetailResponse> allRooms = new ArrayList<>();
            allRooms.addAll(needCleanPage.getContent());
            allRooms.addAll(cleaningPage.getContent());
            roomsPage = new PageImpl<>(allRooms, pageable, needCleanPage.getTotalElements() + cleaningPage.getTotalElements());
        } else {
            tab = "checkin";
            Page<OrderDetailResponse> reservedPage = checkingService.getReservedOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> checkingInPage = checkingService.getCheckingInOrders(PageRequest.of(0, 100));
            Page<OrderDetailResponse> customerConfirmPage = checkingService.getCustomerConfirmedOrders(PageRequest.of(0, 100));
            List<OrderDetailResponse> allCheckinRooms = new ArrayList<>();
            allCheckinRooms.addAll(reservedPage.getContent());
            allCheckinRooms.addAll(checkingInPage.getContent());
            allCheckinRooms.addAll(customerConfirmPage.getContent());
            roomsPage = new PageImpl<>(allCheckinRooms, pageable, reservedPage.getTotalElements() + checkingInPage.getTotalElements() + customerConfirmPage.getTotalElements());
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
}

