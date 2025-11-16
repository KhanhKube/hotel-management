package hotel.rest.checking;

import hotel.db.dto.checking.*;
import hotel.db.entity.User;
import hotel.service.checking.CheckingService;
import hotel.util.MessageResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import static hotel.db.enums.Constants.*;
import static hotel.db.enums.Constants.ADMIN;

@RestController
@RequestMapping("/api/checking")
@RequiredArgsConstructor
public class CheckingController {

    private final CheckingService checkingService;

    // ===== CHECK-IN ENDPOINTS =====

    /**
     * Lấy danh sách order detail với status RESERVED (màn check-in) - CHỈ LỄ TÂN
     */
    @GetMapping("/reserved")
    public ResponseEntity<?> getReservedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getReservedOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách đặt phòng: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách order detail với status CHECKING_IN (khách chờ xác nhận) - CHỈ KHÁCH HÀNG
     */
    @GetMapping("/checking-in")
    public ResponseEntity<?> getCheckingInOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getCheckingInOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách đang check-in: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách order detail với status CUSTOMER_CONFIRMED (chờ staff xác nhận) - CHỈ NHÂN VIÊN
     */
    @GetMapping("/customer-confirmed")
    public ResponseEntity<?> getCustomerConfirmedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getCustomerConfirmedOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách chờ xác nhận: " + e.getMessage()));
        }
    }

    /**
     * Bước 1: Lễ tân bắt đầu check-in: RESERVED -> CHECKING_IN
     */
    @PostMapping("/start-checkin")
    public ResponseEntity<?> startCheckIn(@RequestBody CheckInRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            OrderDetailResponse response = checkingService.startCheckIn(request);
            
            // Kiểm tra xem còn phòng RESERVED không
            long remainingReserved = checkingService.getReservedOrders(PageRequest.of(0, 1)).getTotalElements();
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", remainingReserved > 0 ? "checkin-receptionist" : "checkin-customer")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi bắt đầu check-in: " + e.getMessage()));
        }
    }

    /**
     * Bước 2: Khách hàng xác nhận: CHECKING_IN -> CUSTOMER_CONFIRM
     */
    @PostMapping("/customer-confirm")
    public ResponseEntity<?> customerConfirmCheckIn(@RequestBody CheckInConfirmRequest request) {
        try {
            request.setConfirmedBy("CUSTOMER");
            OrderDetailResponse response = checkingService.confirmCheckIn(request);
            
            // Kiểm tra xem còn phòng CHECKING_IN không
            long remainingCheckingIn = checkingService.getCheckingInOrders(PageRequest.of(0, 1)).getTotalElements();
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", remainingCheckingIn > 0 ? "checkin-customer" : "checkin-staff")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi khách xác nhận check-in: " + e.getMessage()));
        }
    }
    
    /**
     * Bước 3: Nhân viên xác nhận: CUSTOMER_CONFIRM -> OCCUPIED
     */
    @PostMapping("/staff-confirm")
    public ResponseEntity<?> staffConfirmCheckIn(@RequestBody CheckInConfirmRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            request.setConfirmedBy("STAFF");
            OrderDetailResponse response = checkingService.confirmCheckIn(request);
            
            // Kiểm tra xem còn phòng CUSTOMER_CONFIRMED không
            long remainingCustomerConfirmed = checkingService.getCustomerConfirmedOrders(PageRequest.of(0, 1)).getTotalElements();
            
            // Nếu hết check-in, chuyển sang check-out
            if (remainingCustomerConfirmed == 0) {
                long occupiedCount = checkingService.getOccupiedOrders(PageRequest.of(0, 1)).getTotalElements();
                return ResponseEntity.ok()
                        .header("X-Redirect-Tab", occupiedCount > 0 ? "checkout-receptionist" : "checkin-staff")
                        .body(response);
            }
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", "checkin-staff")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi nhân viên xác nhận check-in: " + e.getMessage()));
        }
    }

    // ===== CHECK-OUT ENDPOINTS =====

    /**
     * Lấy danh sách phòng đang ở (OCCUPIED)
     */
    @GetMapping("/occupied")
    public ResponseEntity<?> getOccupiedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getOccupiedOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách phòng đang ở: " + e.getMessage()));
        }
    }

    /**
     * Bước 1: Lễ tân khởi tạo checkout: OCCUPIED -> NEED_CHECKOUT
     */
    @PostMapping("/initiate-checkout")
    public ResponseEntity<?> startCheckOut(@RequestBody CheckOutRequest request) {
        try {
            OrderDetailResponse response = checkingService.startCheckOut(request);
            
            // Kiểm tra xem còn phòng OCCUPIED không
            long remainingOccupied = checkingService.getOccupiedOrders(PageRequest.of(0, 1)).getTotalElements();
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", remainingOccupied > 0 ? "checkout-receptionist" : "checkout-staff")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi bắt đầu check-out: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách phòng NEED_CHECKOUT cho nhân viên
     */
    @GetMapping("/need-checkout")
    public ResponseEntity<?> getNeedCheckOutOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getNeedCheckOutOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách cần check-out: " + e.getMessage()));
        }
    }

    /**
     * Bước 2: Nhân viên kiểm tra phòng: NEED_CHECKOUT -> CHECKING_OUT -> CHECKED_OUT
     */
    @PostMapping("/staff-check-room")
    public ResponseEntity<?> staffCheckOut(@RequestBody StaffCheckOutRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            OrderDetailResponse response = checkingService.staffCheckOut(request);
            
            // Kiểm tra xem còn phòng NEED_CHECKOUT không
            long remainingNeedCheckout = checkingService.getNeedCheckOutOrders(PageRequest.of(0, 1)).getTotalElements();
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", remainingNeedCheckout > 0 ? "checkout-staff" : "checkout-receptionist-confirm")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi nhân viên kiểm tra check-out: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách phòng CHECKED_OUT cho lễ tân xác nhận
     */
    @GetMapping("/checked-out")
    public ResponseEntity<?> getCheckedOutOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getCheckedOutOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách đã check-out: " + e.getMessage()));
        }
    }

    /**
     * Bước 3: Lễ tân xác nhận checkout: CHECKED_OUT -> NEED_CLEAN
     */
    @PostMapping("/confirm-checkout")
    public ResponseEntity<?> receptionistConfirmCheckOut(@RequestBody AfterCheckOutConfirmRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }

            OrderDetailResponse response = checkingService.receptionistConfirmCheckOut(request);
            
            // Kiểm tra xem còn phòng CHECKED_OUT không
            long remainingCheckedOut = checkingService.getCheckedOutOrders(PageRequest.of(0, 1)).getTotalElements();
            
            // Nếu hết, chuyển sang after-checkout (cleaning)
            if (remainingCheckedOut == 0) {
                long needCleanCount = checkingService.getNeedCleanOrders(PageRequest.of(0, 1)).getTotalElements();
                return ResponseEntity.ok()
                        .header("X-Redirect-Tab", needCleanCount > 0 ? "aftercheckout-staff" : "checkout-receptionist-confirm")
                        .body(response);
            }
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", "checkout-receptionist-confirm")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lễ tân xác nhận: " + e.getMessage()));
        }
    }

    // ===== AFTER CHECK-OUT ENDPOINTS =====

    /**
     * Lấy danh sách phòng NEED_CLEAN - CHỈ NHÂN VIÊN
     */
    @GetMapping("/need-clean")
    public ResponseEntity<?> getNeedCleanOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getNeedCleanOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách cần dọn dẹp: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy danh sách phòng CLEANING (đang dọn) - CHỈ NHÂN VIÊN
     */
    @GetMapping("/cleaning")
    public ResponseEntity<?> getCleaningOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<OrderDetailResponse> orders = checkingService.getCleaningOrders(pageable);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi lấy danh sách đang dọn dẹp: " + e.getMessage()));
        }
    }

    /**
     * Bước 4: Nhân viên bắt đầu dọn phòng: NEED_CLEAN -> CLEANING
     */
    @PostMapping("/start-cleaning")
    public ResponseEntity<?> startCleaning(@RequestBody CleaningRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            OrderDetailResponse response = checkingService.startCleaning(request);
            
            // Kiểm tra xem còn phòng NEED_CLEAN không
            long remainingNeedClean = checkingService.getNeedCleanOrders(PageRequest.of(0, 1)).getTotalElements();
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", remainingNeedClean > 0 ? "aftercheckout-staff" : "aftercheckout-cleaning")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi bắt đầu dọn dẹp: " + e.getMessage()));
        }
    }

    /**
     * Bước 5: Nhân viên hoàn thành dọn phòng: CLEANING -> COMPLETED, Room -> AVAILABLE
     */
    @PostMapping("/complete-cleaning")
    public ResponseEntity<?> completeCleaning(@RequestBody CleaningRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Phải đăng nhập trước!"));
            }
            if (user.getRole().equals(CUSTOMER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            if (user.getRole().equals(ADMIN) ||
                    user.getRole().equals(MANAGER)) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(false, "Bạn không có quyền!"));
            }
            OrderDetailResponse response = checkingService.completeCleaning(request);
            
            // Kiểm tra xem còn phòng CLEANING không
            long remainingCleaning = checkingService.getCleaningOrders(PageRequest.of(0, 1)).getTotalElements();
            
            // Nếu hết cleaning, kiểm tra xem còn need clean không
            if (remainingCleaning == 0) {
                long needCleanCount = checkingService.getNeedCleanOrders(PageRequest.of(0, 1)).getTotalElements();
                return ResponseEntity.ok()
                        .header("X-Redirect-Tab", needCleanCount > 0 ? "aftercheckout-staff" : "completed")
                        .body(response);
            }
            
            return ResponseEntity.ok()
                    .header("X-Redirect-Tab", "aftercheckout-cleaning")
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(false, "Lỗi khi hoàn thành dọn dẹp: " + e.getMessage()));
        }
    }
}
