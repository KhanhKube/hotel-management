package hotel.db.dto.floor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorResponseDto {
    
    private Long floorId;
    private Integer floorNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
