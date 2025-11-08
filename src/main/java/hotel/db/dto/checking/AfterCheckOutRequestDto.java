package hotel.db.dto.checking;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request cho after check-out (dọn dẹp phòng)
 * orderDetailId: ID booking
 * readyForNextGuest: true nếu phòng sẵn sàng cho khách mới, false nếu cần bảo trì
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AfterCheckOutRequestDto {
    
    @NotNull(message = "Order Detail ID không được để trống")
    private Integer orderDetailId;
    
    @NotNull(message = "Trạng thái phòng không được để trống")
    private Boolean readyForNextGuest;
}



