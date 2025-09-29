package hotel.db.dto.discount;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountRequestDto {
    private String code;
    private Long orderId;

    public DiscountRequestDto(String code, Long orderId) {
        this.code = code;
        this.orderId = orderId;
    }
}
