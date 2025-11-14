package hotel.db.dto.revenue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueDto {
    private Integer orderId;
    private Long paymentOrderCode;
    private BigDecimal totalAmount;
    private LocalDateTime completedAt;
    private String customerName;
    private String roomNumbers;
    private Integer numberOfRooms;
}
