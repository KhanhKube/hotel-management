package hotel.service.size;

import hotel.db.dto.size.SizeRequestDto;
import hotel.db.dto.size.SizeResponseDto;
import hotel.db.entity.Size;
import java.util.List;

public interface SizeService {
    List<Size> getAllSizes();
    List<SizeResponseDto> getAllActiveSizes();
    Size getSizeById(Integer id);
    SizeResponseDto getSizeByIdDto(Integer id);
    SizeResponseDto createSize(SizeRequestDto sizeRequestDto);
    SizeResponseDto updateSize(Integer id, SizeRequestDto sizeRequestDto);
    Size saveSize(Size size);
    void deleteSize(Integer id);
    boolean existsById(Integer id);
    boolean existsBySize(Double size);
    List<SizeResponseDto> getSizesByRange(Double minSize, Double maxSize);
}
