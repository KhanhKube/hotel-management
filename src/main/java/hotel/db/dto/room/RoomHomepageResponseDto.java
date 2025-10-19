package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomHomepageResponseDto {
    private Integer roomId;
    private String imageRoom;
    private String roomType;
    private BigDecimal price;
    private String roomDescription;
}
