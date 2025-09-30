package hotel.db.dto.checking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckOutFinalizeRequestDto {
    @NotNull private Long bookingId;

    private Double extraFee;       // phụ phí/thiệt hại
    private String note;

    @NotNull private Double amount; // số tiền thu
    @NotNull private String method; // CASH/CARD/TRANSFER...
}
