package hotel.db.dto.room;

import hotel.db.dto.floor.FloorResponseDto;
import hotel.db.dto.size.SizeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDto {

    private Long roomId;
    private String roomNumber;
    private String roomType;
    private String bedType;
    private FloorResponseDto floor;
    private SizeResponseDto size;
    private String roomDescription;
    private BigDecimal price;
    private String status;
    private Integer sold;
    private Integer view;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
