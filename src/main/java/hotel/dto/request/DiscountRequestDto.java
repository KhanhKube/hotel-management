package hotel.dto.request;

public class DiscountRequestDto {
    private String code;
    private Long orderId;

    public DiscountRequestDto(String code, Long orderId) {
        this.code = code;
        this.orderId = orderId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
