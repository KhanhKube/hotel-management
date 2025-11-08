package hotel.db.dto.checking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO hiển thị thông tin booking
 * Dùng cho cả check-in, check-out và after check-out
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    
    private Integer orderDetailId;
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate checkInDate;
    private LocalDate expectedCheckOutDate;
    private LocalDate actualCheckOutDate;
    private String status;
    private String roomStatus;
    private LocalDateTime createdAt;
}



