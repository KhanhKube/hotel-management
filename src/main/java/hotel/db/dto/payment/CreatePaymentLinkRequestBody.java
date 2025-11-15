package hotel.db.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkRequestBody {
	private String productName;
	private String description;
	private String returnUrl;
	private long price;
	private String cancelUrl;
	private Long discountId; // ID của voucher được chọn (nullable) - deprecated, use discountCode
	private String discountCode; // Mã giảm giá (nullable)
	private List<Integer> selectedOrderIds; // Danh sách order IDs được chọn để thanh toán
}
