package hotel.db.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoDto {
    private Integer orderId;
    private Integer orderDetailId;
    private LocalDateTime checkIn;  // Ngày đặt
    private LocalDateTime checkOut; // Ngày hết hạn
    private String roomNumber;
    private String roomType;
    private String status;
    private LocalDateTime createdAt;
}
