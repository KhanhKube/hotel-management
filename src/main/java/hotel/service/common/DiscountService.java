package hotel.service.common;
import hotel.db.entity.Discount;
import hotel.repository.DiscountRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

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
