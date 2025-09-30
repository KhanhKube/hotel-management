package hotel.db.dto.checking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckOutStaffConfirmDto {
    @NotNull private Long bookingId;
    private boolean ok;   // staff chọn Yes/No
    private String note;
}
