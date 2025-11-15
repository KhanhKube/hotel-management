package hotel.db.dto.checking;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderMaintenanceResponse {
    private Integer orderDetailId;
    private Integer orderId;
    private Integer userId;
    private Integer roomId;
    private String roomNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal amount;

    // Customer info
    private String customerName;
    private String customerPhone;
}
