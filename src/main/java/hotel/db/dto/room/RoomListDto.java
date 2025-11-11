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
    private Integer floorNumber;  // Số tầng (1, 2, 3...)
    private Double size;          // Diện tích (25.0, 35.0...)
    private BigDecimal price;
    private String status;
    private String systemStatus;
}
