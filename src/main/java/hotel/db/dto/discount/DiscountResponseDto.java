package hotel.db.dto.discount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DiscountResponseDto {
    private Long discountId;
    private String code;
    private String discountType;
    private Double value;
    private String roomType;
    private LocalDate startDate;
    private LocalDate endDate;
}


