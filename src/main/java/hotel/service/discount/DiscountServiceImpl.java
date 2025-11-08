package hotel.service.discount;
import hotel.db.entity.Discount;
import hotel.db.dto.discount.DiscountResponseDto;
import hotel.db.enums.RoomType;
import hotel.db.repository.discount.DiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public boolean checkVoucherCodeExist(String code) {
        return discountRepository.existsByCodeAndIsDeletedFalse(code);
    }

//    @Override
//    public Discount findDiscountById(Long id) {
//        return discountRepository.findById(id).orElse(null);
//    }

    @Override
    public boolean checkDiscountCodeExistExceptItSelft(String code, Long discountId) {
        return discountRepository.existsByCodeAndDiscountIdNotAndIsDeletedFalse(code, discountId);
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
    
    @Override
    public Page<DiscountResponseDto> getDiscountListForManagement(String search, String discountType, String roomType,
                                                                  String status, String sortBy, int page, int pageSize) {
        List<Discount> discounts = discountRepository.findAllByIsDeletedFalse();
        
        // Filter theo search (mã voucher)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            discounts = discounts.stream()
                    .filter(d -> d.getCode().toLowerCase().contains(searchLower))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter theo discount type
        if (discountType != null && !discountType.isEmpty()) {
            discounts = discounts.stream()
                    .filter(d -> d.getDiscountType().equalsIgnoreCase(discountType))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter theo room type
        if (roomType != null && !roomType.isEmpty()) {
            discounts = discounts.stream()
                    .filter(d -> d.getRoomType().equals(roomType))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter theo status
        if (status != null && !status.isEmpty()) {
            discounts = discounts.stream()
                    .filter(d -> calculateStatus(d).equals(status))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // Sort
        if (sortBy != null && !sortBy.isEmpty()) {
            String[] sortParams = sortBy.split(",");
            String field = sortParams[0];
            String direction = sortParams.length > 1 ? sortParams[1] : "asc";
            
            java.util.Comparator<Discount> comparator = null;
            
            if ("code".equals(field)) {
                comparator = java.util.Comparator.comparing(Discount::getCode);
            } else if ("value".equals(field)) {
                comparator = java.util.Comparator.comparing(Discount::getValue);
            } else if ("startDate".equals(field)) {
                comparator = java.util.Comparator.comparing(Discount::getStartDate);
            }
            
            if (comparator != null) {
                if ("desc".equals(direction)) {
                    comparator = comparator.reversed();
                }
                discounts.sort(comparator);
            }
        }
        
        // Convert to DTO
        List<DiscountResponseDto> discountDtos = discounts.stream()
                .map(this::getListDiscountDto)
                .collect(java.util.stream.Collectors.toList());
        
        // Pagination thủ công
        int startPage = page * pageSize;
        int endPage = Math.min(startPage + pageSize, discountDtos.size());
        
        List<DiscountResponseDto> pagedDtos = discountDtos.subList(startPage, endPage);
        
        return new org.springframework.data.domain.PageImpl<>(pagedDtos, 
                org.springframework.data.domain.PageRequest.of(page, pageSize), 
                discountDtos.size());
    }

}