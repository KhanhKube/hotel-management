package hotel.db.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    private Integer orderId;
    private Integer roomId;
    private String roomType;
    private String roomNumber;
    private BigDecimal price;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private Integer numberOfDays;
    private BigDecimal totalPrice;
    private String imageRoom;
}
