package hotel.service.checking;

import hotel.db.dto.checking.AfterCheckOutRequestDto;
import hotel.db.dto.checking.BookingDto;
import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service xử lý check-in, check-out và after check-out
 * 
 * Flow chuẩn:
 * 1. PENDING/CART → Check-in → CHECKED_IN (Room: OCCUPIED)
 * 2. CHECKED_IN → Check-out → CHECKED_OUT (Room: CLEANING)
 * 3. CHECKED_OUT → After check-out → COMPLETED (Room: AVAILABLE hoặc MAINTENANCE)
 */
public interface CheckingService {
    
    // ===== CHECK-IN =====
    
    /**
     * Xử lý check-in: PENDING/CART → CHECKED_IN
     * Room: AVAILABLE → OCCUPIED
     */
    void checkIn(CheckInRequestDto request);
    
    /**
     * Lấy danh sách booking chờ check-in (PENDING, CART) và đã check-in (CHECKED_IN)
     */
    Page<BookingDto> getCheckInList(Pageable pageable);
    
    /**
     * Đếm số lượng booking chờ check-in và đã check-in
     */
    long countCheckInItems();
    
    // ===== CHECK-OUT =====
    
    /**
     * Xử lý check-out: CHECKED_IN → CHECKED_OUT
     * Room: OCCUPIED → CLEANING
     */
    void checkOut(CheckOutRequestDto request);
    
    /**
     * Lấy danh sách booking chờ check-out (CHECKED_IN)
     */
    Page<BookingDto> getCheckOutList(Pageable pageable);
    
    /**
     * Đếm số lượng booking chờ check-out
     */
    long countCheckOutItems();
    
    // ===== AFTER CHECK-OUT =====
    
    /**
     * Xử lý after check-out: CHECKED_OUT → COMPLETED
     * Room: CLEANING → AVAILABLE (nếu readyForNextGuest = true) hoặc MAINTENANCE (nếu false)
     */
    void afterCheckOut(AfterCheckOutRequestDto request);
    
    /**
     * Lấy danh sách phòng cần dọn dẹp (Room: CLEANING, OrderDetail: CHECKED_OUT)
     */
    Page<BookingDto> getAfterCheckOutList(Pageable pageable);
    
    /**
     * Đếm số lượng phòng cần dọn dẹp
     */
    long countAfterCheckOutItems();
}

