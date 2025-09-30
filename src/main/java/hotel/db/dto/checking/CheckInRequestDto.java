package hotel.db.dto.checking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CheckInRequestDto {
    @NotNull private Integer roomId;
    @NotBlank private String customerName;

    private String customerPhone;
    private String customerEmail;

    private Double expectedPrice; // giá dự kiến
    private String note;

    private List<String> services; // dịch vụ kèm
}
