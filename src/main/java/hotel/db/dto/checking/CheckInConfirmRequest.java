package hotel.db.dto.checking;

import lombok.Data;

@Data
public class CheckInConfirmRequest {
    private Integer orderDetailId;
    private String confirmedBy; // "CUSTOMER" hoặc "STAFF"
}
