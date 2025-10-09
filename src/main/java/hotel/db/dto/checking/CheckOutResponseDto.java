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
public class CheckOutResponseDto {

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
    private LocalDate actualCheckOutDate;
    private Integer numberOfGuests;
    private Double totalAmount;
    private Double penaltyAmount;
    private String penaltyReason;
    private String notes;
    private String paymentMethod;
    private String roomCondition;
    private Boolean hasDamage;
    private String damageDescription;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}