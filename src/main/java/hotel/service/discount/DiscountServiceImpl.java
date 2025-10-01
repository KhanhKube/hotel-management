package hotel.service.discount;
import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;

import hotel.db.repository.discount.DiscountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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
        if (discount.getStatus() != Discount.Status.ACTIVE) {
            throw new RuntimeException("Voucher không khả dụng");
        }

        // kiểm tra số lần dùng
        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new RuntimeException("Voucher đã được dùng hết số lần cho phép");
        }

        return discount;
    }

    private DiscountResponseDto toDto(Discount d) {
        return new DiscountResponseDto(
                d.getDiscountId(),
                d.getCode(),
                d.getDiscountType(),
                d.getValue(),
                d.getRoomType(),
                d.getStartDate(),
                d.getEndDate(),
                d.getStatus()
        );
    }

    @Override
    public List<DiscountResponseDto> getAll() {
        return discountRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<DiscountResponseDto> getAll(Pageable pageable) {
        return discountRepository.findAll(pageable)
                .map(this::toDto);
    }
}
