package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class RoomListDto {
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Integer floorNumber;
    private Double sizeId;
    private BigDecimal price;
    private String status;
}
