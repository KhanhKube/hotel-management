package hotel.rest.discount;

import hotel.db.dto.discount.DiscountRequestDto;
import hotel.db.entity.Discount;
import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/discounts")
public class DiscountController {

	private final DiscountService discountService;

	//test
	@PostMapping("/apply")
	public String applyVoucher(@RequestBody DiscountRequestDto request) {
		Discount discount = discountService.applyVoucher(request.getCode());
		return "Áp dụng voucher thành công: " + discount.getCode() +
				" (" + discount.getDiscountType() + " - " + discount.getValue() + ")";
	}
}
