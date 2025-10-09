package hotel.db.dto.checking;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOutRequestDto {

    @NotNull(message = "Booking ID không được để trống")
    private Integer bookingId;

    @NotNull(message = "Ngày check-out thực tế không được để trống")
    private LocalDate actualCheckOutDate;

    @NotNull(message = "Tổng tiền thanh toán không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền thanh toán không được âm")
    private Double totalAmount;

    @DecimalMin(value = "0.0", message = "Tiền phạt không được âm")
    private Double penaltyAmount;

    private String penaltyReason;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    @NotBlank(message = "Tình trạng phòng không được để trống")
    private String roomCondition;

    private Boolean hasDamage;

    private String damageDescription;

    private String notes;
}