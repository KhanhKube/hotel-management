package hotel.service.discount;

import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DiscountService {
	 Discount applyVoucher(String code);

	 java.util.List<DiscountResponseDto> getAll();

	 Page<DiscountResponseDto> getAll(Pageable pageable);
}
