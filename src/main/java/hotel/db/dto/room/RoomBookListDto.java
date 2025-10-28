package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomBookListDto {
    private Integer roomId;
    private String roomNumber;
    private String imageRoom;
    private String roomType;
    private BigDecimal price;
    private String roomDescription;

    // Chỉ 2 trường hợp: "Trống" hoặc "Có thể đặt từ dd/MM/yyyy"
    private String statusDisplay;

    private LocalDate availableFrom; //Lấy được thời giản available booking.
}
