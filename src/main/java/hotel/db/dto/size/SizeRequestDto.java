package hotel.db.dto.size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeRequestDto {

    @NotNull(message = "Kích thước không được để trống")
    @DecimalMin(value = "0.1", message = "Kích thước phải lớn hơn 0")
    @DecimalMax(value = "1000.0", message = "Kích thước không được vượt quá 1000")
    private Double size;
}