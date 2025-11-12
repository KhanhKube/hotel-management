package hotel.db.dto.checking;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderDetailResponse {
    private Integer orderDetailId;
    private Integer orderId;
    private Integer userId;
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Integer floorId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String orderDescription;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private String roomStatus;
    
    // Customer info
    private String customerName;
    private String customerPhone;
}
