package hotel.service.checking;

import hotel.db.dto.checking.CheckInRequestDto;
import hotel.db.dto.checking.CheckInResponseDto;
import hotel.db.dto.checking.CheckOutRequestDto;
import hotel.db.dto.checking.CheckOutResponseDto;

import java.util.List;

public interface CheckService {

    /**
     * Lấy danh sách tất cả check-in đang hoạt động
     */
    List<CheckInResponseDto> getAllActiveCheckIns();

    /**
     * Lấy danh sách booking có thể check-out
     */
    List<CheckOutResponseDto> getCheckOutCandidates();

    /**
     * Lấy danh sách phòng trống
     */
    List<Object> getAvailableRooms();

    /**
     * Lấy danh sách khách hàng
     */
    List<Object> getCustomers();

    /**
     * Lấy thông tin booking theo ID
     */
    CheckInResponseDto getBookingById(Integer bookingId);

    /**
     * Thực hiện check-in
     */
    CheckInResponseDto checkIn(CheckInRequestDto checkInRequestDto);

    /**
     * Thực hiện check-out
     */
    CheckOutResponseDto checkOut(CheckOutRequestDto checkOutRequestDto);

    /**
     * Kiểm tra phòng có sẵn không
     */
    boolean isRoomAvailable(Integer roomId, String checkInDate, String checkOutDate);

    /**
     * Lấy lịch sử check-in/check-out
     */
    List<CheckOutResponseDto> getCheckInOutHistory();

    /**
     * Lấy thông tin check-in theo ID
     */
    CheckInResponseDto getCheckInById(Integer bookingId);
}