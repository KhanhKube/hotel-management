package hotel.db.dto.checking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request cho check-in
 * Chỉ cần orderDetailId
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInRequestDto {
    
    @NotNull(message = "Order Detail ID không được để trống")
    private Integer orderDetailId;
}

