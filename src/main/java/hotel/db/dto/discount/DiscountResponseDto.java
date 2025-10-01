package hotel.db.dto.discount;

import hotel.db.entity.Discount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DiscountResponseDto {
    private Long discountId;
    private String code;
    private Discount.DiscountType discountType;
    private Double value;
    private String roomType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Discount.Status status;
}


