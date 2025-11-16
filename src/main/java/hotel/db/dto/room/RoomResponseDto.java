package hotel.db.dto.room;

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
	private Integer floorNumber;
	private Double size;
	private String roomDescription;
	private BigDecimal price;
	private String status;
	private Integer sold;
	private Integer view;
	private Integer max_size_people;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Boolean isDeleted;
}
