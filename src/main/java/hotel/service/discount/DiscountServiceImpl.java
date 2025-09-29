package hotel.service.discount;
import hotel.db.entity.Discount;

import hotel.db.repository.discount.DiscountRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Discount applyVoucher(String code) {
        Discount discount = discountRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // kiểm tra hạn sử dụng
        if (discount.getEndDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Voucher đã hết hạn");
        }

        // kiểm tra trạng thái
        if (!"ACTIVE".equals(discount.getStatus())) {
            throw new RuntimeException("Voucher không khả dụng");
        }

        // kiểm tra số lần dùng
        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new RuntimeException("Voucher đã được dùng hết số lần cho phép");
        }

        return discount;
    }
}
