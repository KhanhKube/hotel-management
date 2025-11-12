package hotel.db.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
	private Integer orderId;
	private Integer userId;
	private LocalDateTime checkIn;
	private LocalDateTime checkOut;
	private String status;
	private LocalDateTime createdAt;
	private Long paymentOrderCode;
	private BigDecimal totalAmount;
	private List<OrderDetailDto> orderDetails;
}
