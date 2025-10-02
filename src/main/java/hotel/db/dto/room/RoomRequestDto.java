package hotel.db.dto.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequestDto {

    @NotBlank(message = "Số phòng không được để trống")
    @Size(max = 50, message = "Số phòng không được vượt quá 50 ký tự")
    private String roomNumber;

    @Size(max = 100, message = "Loại phòng không được vượt quá 100 ký tự")
    private String roomType;

    @Size(max = 100, message = "Loại giường không được vượt quá 100 ký tự")
    private String bedType;

    @NotNull(message = "Tầng không được để trống")
    private Long floorId;

    @NotNull(message = "Kích thước không được để trống")
    private Long sizeId;

    private String roomDescription;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phòng phải lớn hơn 0")
    @Digits(integer = 8, fraction = 2, message = "Giá phòng không hợp lệ")
    private BigDecimal price;

    @Size(max = 50, message = "Trạng thái không được vượt quá 50 ký tự")
    private String status;

    @Min(value = 0, message = "Số lượng đã bán không được âm")
    private Integer sold;

    @Min(value = 0, message = "Số lượt xem không được âm")
    private Integer view;
}
