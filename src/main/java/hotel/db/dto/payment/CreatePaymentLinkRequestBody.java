package hotel.db.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkRequestBody {
	private String productName;
	private String description;
	private String returnUrl;
	private long price;
	private String cancelUrl;
	private Long discountId; // ID của voucher được chọn (nullable)
}
