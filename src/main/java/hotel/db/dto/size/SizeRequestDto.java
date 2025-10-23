package hotel.db.dto.size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SizeRequestDto {

    @NotNull(message = "Kích thước không được để trống")
    @DecimalMin(value = "0.01", message = "Kích thước phải lớn hơn 0")
    @DecimalMax(value = "999.99", message = "Kích thước không được vượt quá 999.99")
    private Double size;
}
