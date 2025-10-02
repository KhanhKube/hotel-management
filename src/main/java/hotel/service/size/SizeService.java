package hotel.service.size;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface SizeService {

    /**
     * Lấy tất cả size đang hoạt động
     */
    List<SizeResponseDto> getAllActiveSizes();

    /**
     * Lấy size theo ID
     */
    SizeResponseDto getSizeById(Long sizeId);

    /**
     * Tạo size mới
     */
    SizeResponseDto createSize(SizeRequestDto sizeRequestDto);

    /**
     * Cập nhật size
     */
    SizeResponseDto updateSize(Long sizeId, SizeRequestDto sizeRequestDto);

    /**
     * Xóa mềm size
     */
    void deleteSize(Long sizeId);

    /**
     * Kiểm tra size có tồn tại không
     */
    boolean existsById(Long sizeId);

    /**
     * Kiểm tra giá trị size đã tồn tại chưa
     */
    boolean existsBySize(BigDecimal size);

    /**
     * Tìm size trong khoảng giá trị
     */
    List<SizeResponseDto> getSizesByRange(BigDecimal minSize, BigDecimal maxSize);
}
