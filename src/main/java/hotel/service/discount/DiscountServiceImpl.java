package hotel.service.discount;
import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;

import hotel.db.repository.discount.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;

    @Override
    public Discount applyVoucher(String code) {
        Discount discount = discountRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // kiểm tra hạn sử dụng
        if (discount.getEndDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Voucher đã hết hạn");
        }

        // kiểm tra trạng thái

        // kiểm tra số lần dùng
        if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
            throw new RuntimeException("Voucher đã được dùng hết số lần cho phép");
        }

        return discount;
    }
    @Override
    public String calculateStatus(Discount d) {
        LocalDate now = LocalDate.now();
        if (d.getUsedCount() >= d.getUsageLimit()) {
            return "EXHAUSTED"; // Hết số lượng
        }
        if (d.getEndDate().isBefore(now)) {
            return "EXPIRED"; // Hết hạn
        }
        if (d.getStartDate().isAfter(now)) {
            return "PENDING"; // Chưa bắt đầu
        }
        return "ACTIVE"; // Đang HD
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
                calculateStatus(d)
        );
    }

    @Override
    public List<DiscountResponseDto> getAll() {
        List<Discount> entities = discountRepository.findAllByIsDeletedFalse();
        List<DiscountResponseDto> result = new ArrayList<>();
        for (Discount d : entities) {
            result.add(toDto(d)); // toDto nhận Discount, trả DiscountResponseDto
        }
        return result;
    }

    @Override
    public Page<DiscountResponseDto> getAll(Pageable pageable) {
        return discountRepository.findAll(pageable)
                .map(this::toDto);
    }
}
