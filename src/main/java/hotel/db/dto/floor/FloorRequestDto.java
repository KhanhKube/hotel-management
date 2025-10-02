package hotel.db.dto.floor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorRequestDto {

    @NotNull(message = "Số tầng không được để trống")
    @Positive(message = "Số tầng phải là số dương")
    private Integer floorNumber;
}
