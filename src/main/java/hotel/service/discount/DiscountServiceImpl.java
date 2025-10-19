package hotel.service.discount;
import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;
import hotel.db.enums.RoomType;
import hotel.db.repository.discount.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;

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

    private DiscountResponseDto getListDiscountDto(Discount d) {
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
    public List<DiscountResponseDto> getListDiscount() {
        List<Discount> entities = discountRepository.findAllByIsDeletedFalse();
        List<DiscountResponseDto> result = new ArrayList<>();
        for (Discount d : entities) {
            result.add(getListDiscountDto(d)); // toDto nhận Discount, trả DiscountResponseDto
        }
        return result;
    }

    @Override
    public Discount getDiscountById(Long discountId) {
        return discountRepository.findDiscountByDiscountId(discountId);
    }

    @Override
    public List<String> getRoomTypesForDiscount() {
        return Arrays.asList(RoomType.ALL); // Chỉ trả về các loại phòng từ enum, không có "Tất cả"
    }

}