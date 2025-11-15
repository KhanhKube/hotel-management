package hotel.db.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {
    private Integer roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String description;
}
