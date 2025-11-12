package hotel.db.dto.furnishing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FurnishingFormDto {
    private Integer furnishingId;
    private String name;
    private Integer stockQuantity; // Số lượng tồn kho
    private Integer roomQuantity;  // Số lượng phòng sẽ có (input từ form)
}
