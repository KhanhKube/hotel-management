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
public class CheckInRequestDto {

    @NotNull(message = "Room ID không được để trống")
    private Integer roomId;

    @NotNull(message = "Customer ID không được để trống")
    private Integer customerId;

    @NotNull(message = "Ngày check-in không được để trống")
    private LocalDate checkInDate;

    @NotNull(message = "Ngày check-out dự kiến không được để trống")
    private LocalDate expectedCheckOutDate;

    @NotNull(message = "Số người ở không được để trống")
    @Min(value = 1, message = "Số người ở phải lớn hơn 0")
    @Max(value = 10, message = "Số người ở không được vượt quá 10")
    private Integer numberOfGuests;

    @DecimalMin(value = "0.0", message = "Tiền cọc không được âm")
    private Double depositAmount;

    private String paymentMethod;

    private String services;

    private String notes;
}