package hotel.db.dto.size;

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
public class SizeResponseDto {

	private Integer sizeId;
	private BigDecimal size;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
