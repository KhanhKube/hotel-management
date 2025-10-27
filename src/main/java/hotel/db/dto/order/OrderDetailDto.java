package hotel.db.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Integer orderDetailId;
    private Integer orderId;
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal price;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String status;
    private String orderDescription;
}
