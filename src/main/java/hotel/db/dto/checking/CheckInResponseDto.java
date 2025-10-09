package hotel.db.dto.checking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInResponseDto {

    private Integer bookingId;
    private Integer roomId;
    private String roomNumber;
    private String roomType;
    private Integer customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate checkInDate;
    private LocalDate expectedCheckOutDate;
    private Integer numberOfGuests;
    private String notes;
    private String services;
    private Double depositAmount;
    private String paymentMethod;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}