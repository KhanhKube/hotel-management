package hotel.service.checking;

import hotel.db.dto.checking.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CheckingService {
    
    // ===== CHECK-IN FLOW =====
    
    /**
     * Lấy danh sách order detail với status RESERVED (màn check-in) - CHỈ LỄ TÂN
     */
    Page<OrderDetailResponse> getReservedOrders(Pageable pageable);
    
    /**
     * Lấy danh sách order detail với status CHECKING_IN (khách chờ xác nhận) - CHỈ KHÁCH HÀNG
     */
    Page<OrderDetailResponse> getCheckingInOrders(Pageable pageable);
    
    /**
     * Lấy danh sách order detail với status CUSTOMER_CONFIRMED (chờ staff xác nhận) - CHỈ NHÂN VIÊN
     */
    Page<OrderDetailResponse> getCustomerConfirmedOrders(Pageable pageable);
    
    /**
     * Nhấn check-in: RESERVED -> CHECKING_IN
     */
    OrderDetailResponse startCheckIn(CheckInRequest request);
    
    /**
     * Khách hàng hoặc staff xác nhận: CHECKING_IN -> CUSTOMER_CONFIRMED hoặc OCCUPIED
     */
    OrderDetailResponse confirmCheckIn(CheckInConfirmRequest request);
    
    // ===== CHECK-OUT FLOW =====
    
    /**
     * Lấy danh sách phòng đang ở (OCCUPIED) cho màn check-out
     */
    Page<OrderDetailResponse> getOccupiedOrders(Pageable pageable);
    
    /**
     * Lễ tân nhấn check-out: OCCUPIED -> NEED_CHECKOUT
     */
    OrderDetailResponse startCheckOut(CheckOutRequest request);
    
    /**
     * Lấy danh sách phòng NEED_CHECKOUT cho nhân viên - CHỈ NHÂN VIÊN
     */
    Page<OrderDetailResponse> getNeedCheckOutOrders(Pageable pageable);
    
    /**
     * Lấy danh sách phòng NEED_CLEAN cho nhân viên - CHỈ NHÂN VIÊN
     */
    Page<OrderDetailResponse> getNeedCleanOrdersForStaff(Pageable pageable);
    
    /**
     * Lấy danh sách phòng CLEANING (đang dọn) - CHỈ NHÂN VIÊN
     */
    Page<OrderDetailResponse> getCleaningOrders(Pageable pageable);
    
    /**
     * Nhân viên kiểm tra và gửi form: NEED_CHECKOUT -> CHECKED_OUT
     */
    OrderDetailResponse staffCheckOut(StaffCheckOutRequest request);
    
    /**
     * Lấy danh sách phòng CHECKING_OUT (đang kiểm tra)
     */
    Page<OrderDetailResponse> getCheckingOutOrders(Pageable pageable);
    
    /**
     * Lấy danh sách phòng CHECKED_OUT cho lễ tân xác nhận
     */
    Page<OrderDetailResponse> getCheckedOutOrders(Pageable pageable);
    
    /**
     * Lễ tân xác nhận sau khi nhân viên check: CHECKED_OUT -> NEED_CLEAN
     */
    OrderDetailResponse receptionistConfirmCheckOut(AfterCheckOutConfirmRequest request);
    
    // ===== AFTER CHECK-OUT FLOW =====
    
    /**
     * Lấy danh sách phòng NEED_CLEAN cho màn after check-out
     */
    Page<OrderDetailResponse> getNeedCleanOrders(Pageable pageable);
    
    /**
     * Nhân viên bắt đầu dọn dẹp: NEED_CLEAN -> CLEANING
     */
    OrderDetailResponse startCleaning(CleaningRequest request);
    
    /**
     * Nhân viên hoàn thành dọn dẹp: CLEANING -> COMPLETED, Room -> AVAILABLE
     */
    OrderDetailResponse completeCleaning(CleaningRequest request);
}
