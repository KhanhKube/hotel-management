package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDetailResponseDto {
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private String bedType;
    private Integer floorNumber;
    private Double size;
    private String roomDescription;
    private BigDecimal price;
    private String status;
    private Integer sold;
    private Integer view;
    private List<String> images;
}
