package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomBookListDto {
    private Integer roomId;
    private String roomNumber;
    private String imageRoom;
    private String roomType;
    private BigDecimal price;
    private String roomDescription;
    private List<String> roomViews;
}
