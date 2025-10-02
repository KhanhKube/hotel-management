package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomSearchDto {

    private String roomType;
    private String bedType;
    private Long floorId;
    private Long sizeId;
    private String status;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
