package hotel.rest.discount;

import hotel.db.dto.discount.DiscountRequestDto;
import hotel.db.dto.discount.DiscountResponseDto;
import hotel.db.entity.Discount;
import hotel.service.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping
	public ResponseEntity<java.util.List<DiscountResponseDto>> list() {
		return ResponseEntity.ok(discountService.getAll());
	}

	@GetMapping("/page")
	public ResponseEntity<Page<DiscountResponseDto>> listPage(
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
			Pageable pageable) {
		return ResponseEntity.ok(discountService.getAll(pageable));
	}
}
