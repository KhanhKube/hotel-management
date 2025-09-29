package hotel.service.discount;

import hotel.db.entity.Discount;

public interface DiscountService {
	 Discount applyVoucher(String code);
}
